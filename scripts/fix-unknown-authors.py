# Copyright 2021 Harness Inc.
# 
# Licensed under the Apache License, Version 2.0
# http://www.apache.org/licenses/LICENSE-2.0

import re
import shlex
import subprocess
from collections import Counter
from collections import deque

EMAIL_PATTERN = re.compile(r'<(\S+@(harness\.io|wings\.software))>')
TEST_METHOD_START = re.compile(r'\) {3}public void (\S+)\(')
TEST_METHOD_END = re.compile(r'\) {3}}')

owner_map = {
    "aaditi.joag@harness.io": "AADITI",
    "adwait.bhandare@harness.io": "ADWAIT",
    "aman.singh@harness.io": "AMAN",
    "ankit.singhal@harness.io": "ANKIT",
    "anshul@harness.io": "ANSHUL",
    "anubhaw@harness.io": "ANUBHAW",
    "abhijith.mohan@harness.io": "AVMOHAN",
    "brett@harness.io": "BRETT",
    "deepak.patankar@harness.io": "DEEPAK",
    "garvit.pahal@harness.io": "GARVIT",
    "george@harness.io": "GEORGE",
    "hannah.tang@harness.io": "HANTANG",
    "harsh.jain@harness.io": "HARSH",
    "hitesh.aringa@harness.io": "HITESH",
    "jatin@harness.io": "UJJAWAL",
    "juhi.agrawal@harness.io": "JUHI",
    "kamal.joshi@harness.io": "KAMAL",
    "mark.lu@harness.io": "UTKARSH",
    "meenakshi.raikwar@harness.io": "MEENAKSHI",
    "nataraja@harness.io": "NATARAJA",
    "parnian@harness.io": "PARNIAN",
    "pooja.singhal@harness.io": "POOJA",
    "pranjal@harness.io": "PRANJAL",
    "prashant.pal@harness.io": "PRASHANT",
    "praveen.sugavanam@harness.io": "PRAVEEN",
    "puneet.saraswat@harness.io": "PUNEET",
    "raghu@harness.io": "RAGHU",
    "rama@harness.io": "RAMA",
    "rohit.kumar@harness.io": "ROHIT_KUMAR",
    "rohit.reddy@harness.io": "ROHIT",
    "rushabh.shah@harness.io": "RUSHABH",
    "satyam.shanker@harness.io": "SATYAM",
    "shaswat.deep@harness.io": "SHASWAT",
    "shubhanshu.verma@harness.io": "SHUBHANSHU",
    "sowmya.k@harness.io": "SOWMYA",
    "srinivas@harness.io": "SRINIVAS",
    "sriram@harness.io": "SRIRAM",
    "sunil@harness.io": "NATARAJA",
    "swamy@harness.io": "NATARAJA",
    "ujjawal.prasad@harness.io": "UJJAWAL",
    "utkarsh.gupta@harness.io": "UTKARSH",
    "vaibhav.si@harness.io": "VAIBHAV_SI",
    "vaibhav.tulsyan@harness.io": "UJJAWAL",
    "venkatesh.kotrike@harness.io": "VENKATESH",
    "vikas.naiyar@harness.io": "VIKAS",
    "yogesh.chauhan@harness.io": "YOGESH_CHAUHAN"
}

still_unknown = set()


def get_autotagged_tests():
    # git diff 8e19dea708~2..8e19dea708 -- '**/src/test/**/*Test.java' > autogen-author-diff
    fname_re = re.compile(r'\+{3} b/(.*)')
    tname_re = re.compile(r' {3}public void (\S+)\(')
    autotagged_tests = set()
    with open('autogen-author-diff') as ifile:
        fname, tname = None, None
        for line in ifile:
            fmatch = fname_re.match(line)
            if fmatch:
                fname = fmatch.group(1)
            tmatch = tname_re.match(line)
            if tmatch:
                tname = tmatch.group(1)
                # print("{}:{}".format(fname, tname))
                autotagged_tests.add((fname, tname))
    return autotagged_tests


autotagged_tests = get_autotagged_tests()

def blame_info(src_file):
    cmd = r'''git blame -M -C -w -e {0}'''.format(src_file)
    args = shlex.split(cmd)
    result = subprocess.run(args, capture_output=True, text=True)
    result.check_returncode()
    blame_lines = result.stdout.split('\n')[:-1]
    tail = deque([], 5)
    owner = {}
    in_test_method = False
    for i, line in enumerate(blame_lines):
        try:
            if not in_test_method and TEST_METHOD_START.search(line) and '@Category' in tail[-1][1]:
                tname = TEST_METHOD_START.search(line).group(1)
                print(src_file, tname)
                print("Entering test method:### " + line)
                in_test_method = True
                owner_unknown = False
                owner_autogenerated = (src_file, tname) in autotagged_tests
                contrib = Counter()
                total = 0
                # find line with @Owner annotation
                for tlnum, tline in reversed(tail):
                    if '@Owner(developers =' in tline:
                        owner_lnum = tlnum
                        owner_unknown = True

            if in_test_method:
                email = EMAIL_PATTERN.search(line).group(1)
                # print(email)
                contrib[email] += 1
                total += 1

            if in_test_method and TEST_METHOD_END.search(line):
                # print("Leaving test method:### "  + line)
                in_test_method = False
                if owner_unknown and owner_autogenerated:
                    for email in (e for (e, c) in contrib.most_common()):
                        if contrib[email] * 10 > total and email in owner_map:
                            owner[owner_lnum] = owner_map[email]
                            break
                        else:
                            print(
                                "Author {} rejected as owner with lines ({}/{}))".format(email, contrib[email], total))
                    else:
                        # none of the authors are in owner_map
                        email = contrib.most_common()[0][0]
                        still_unknown.add(email)
                        owner[owner_lnum] = "UNKNOWN"
            tail.append((i, line))
        except Exception as e:
            print(line)
            raise e

    return owner


def process_file(src_file):
    owners = blame_info(src_file)
    if not owners:
        return
    with open(src_file) as ifile:
        orig_lines = list(ifile)
    unique_owners = set(owners.values())
    # print(unique_owners)
    imports_added = False
    last_line = ''
    with open(src_file, 'w') as ofile:
        for lnum, line in enumerate(orig_lines):
            # Add imports
            if not imports_added and last_line.startswith('import') and not line.startswith('import'):
                for owner in unique_owners:
                    ofile.write('import static io.harness.rule.OwnerRule.{0};\n'.format(owner))
                imports_added = True

            if lnum in owners:
                if 'intermittent = true' not in line:
                    ofile.write("  @Owner(developers = {})\n".format(owners[lnum]))
                else:
                    ofile.write("  @Owner(developers = {}, intermittent = true)\n".format(owners[lnum]))
            else:
                ofile.write(line)
            last_line = line


def main():
    # File has list of source file paths to be modified
    # find `pwd` -type f -path '**/src/test/**/*.java' -exec grep -l -E  '@Owner.*developers.*' {} \; | sort > unknown-author-files
    unknown_author_files = 'unknown-author-files'
    try:
        with open(unknown_author_files) as ifile:
            files_to_fix = list(ifile)
        for filename in files_to_fix:
            print("processing file " + filename)
            process_file(filename.strip())
    except BrokenPipeError:
        pass
    print("Below authors still unknown:-")
    print(still_unknown)


if __name__ == "__main__":
    main()
