# Copyright 2021 Harness Inc.
# 
# Licensed under the Apache License, Version 2.0
# http://www.apache.org/licenses/LICENSE-2.0

import sys

from core.distance.LevenShtein import Hirschberg
from core.distance.LevenShtein import LevenShteinDistance
from core.distance.SAXHMMDistance import SAXHMMDistance
from core.util.TimeSeriesUtils import MetricToDeviationType


def get_dist(x, y, a, b):
    return SAXHMMDistance.get_letter_dist(MetricToDeviationType.HIGHER,
                                          SAXHMMDistance.inverted_alphabet[a], SAXHMMDistance.inverted_alphabet[b])


def test_ld_dist():
    ld = LevenShteinDistance(get_dist)
    result, indices = ld.find_optimal_alignment('axxbcxefc', 'bbxcbaxxb')
    assert result == 'bxxbcxbab'


def test_ld_dist_1():
    ld = LevenShteinDistance(get_dist)
    result, indices = ld.find_optimal_alignment('bxb', 'xxb')
    assert result == 'xxb'


def test_ld_dist_2():
    ld = LevenShteinDistance(get_dist)
    result, indices = ld.find_optimal_alignment('bxx', 'bxx')
    assert result == 'bxx'


def test_ld_dist_3():
    ld = LevenShteinDistance(get_dist)
    result, indices = ld.find_optimal_alignment('bxb', 'xbx')
    assert result == 'bxx'


def test_ld_dist_4():
    ld = LevenShteinDistance(get_dist)
    result, indices = ld.find_optimal_alignment('xxb', 'bxx')
    assert result == 'xxb'


def test_ld_dist_5():
    # hb = Hirschberg()
    ld = LevenShteinDistance(get_dist)
    result, indices = ld.find_optimal_alignment('bb', 'xbx')
    assert result == 'bx'

def test_ld_dist_6():
    ld = LevenShteinDistance(get_dist)
    result, indices = ld.find_optimal_alignment('xxeca', 'ffxxx')
    assert result == 'xxffx'


def test_hb_dist():
    # hb = Hirschberg()
    hb = Hirschberg(get_dist)
    result = hb.find_optimal_alignment('axxbcxefc', 'bbxcbaxxb')
    assert result == 'bxxbcxbab'


def test_hb_dist_1():
    # hb = Hirschberg()
    hb = Hirschberg(get_dist)
    result = hb.find_optimal_alignment('bxb', 'xxb')
    assert result == 'xxb'


def test_hb_dist_2():
    # hb = Hirschberg()
    hb = Hirschberg(get_dist)
    result = hb.find_optimal_alignment('bxx', 'bxx')
    assert result == 'bxx'


def test_hb_dist_3():
    # hb = Hirschberg()
    hb = Hirschberg(get_dist)
    result = hb.find_optimal_alignment('bxb', 'xbx')
    assert result == 'bxx'


def test_hb_dist_4():
    # hb = Hirschberg()
    hb = Hirschberg(get_dist)
    result = hb.find_optimal_alignment('xxb', 'bxx')
    assert result == 'xxb'


def test_hb_dist_5():
    # hb = Hirschberg()
    hb = Hirschberg(get_dist)
    result = hb.find_optimal_alignment('bb', 'xbx')
    assert result == 'bx'


def main(args):
    test_ld_dist_6()

if __name__ == "__main__":
    main(sys.argv)
