#!/bin/bash

gradle clean build

# for pf4j-test project
cp plugin-1/build/libs/plugin-1-0.1.jar pf4j-test/lib/
cp plugin-2/build/libs/plugin-2-0.1.jar pf4j-test/lib/


# for pf4j-spring project
cp plugin-1/build/libs/plugin-1-0.1.jar pf4j-spring/lib/
cp plugin-2/build/libs/plugin-2-0.1.jar pf4j-spring/lib/
