#!/bin/bash

# Copyright 2021 Harness Inc.
# 
# Licensed under the Apache License, Version 2.0
# http://www.apache.org/licenses/LICENSE-2.0


source set_default_variables.sh
bash ./replace_configs.sh
exec ./start_process.sh
