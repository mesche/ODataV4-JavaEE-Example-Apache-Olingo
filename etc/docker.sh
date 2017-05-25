#!/bin/sh

#===================================================================
#	              
# 		                  -- VERSION 1.0.0  --
#
#   Startparameter: 
#        build | start | startbg | stop | remove | login
#	
#   Example command:
#        ./docker.sh build
#
#    ::::::::::::::: www.blogging-it.com :::::::::::::::
#    
# Copyright (C) 2017 Markus Eschenbach. All rights reserved.
# 
# 
# This software is provided on an "as-is" basis, without any express or implied warranty.
# In no event shall the author be held liable for any damages arising from the
# use of this software.
# 
# Permission is granted to anyone to use this software for any purpose,
# including commercial applications, and to alter and redistribute it,
# provided that the following conditions are met:
# 
# 1. All redistributions of source code files must retain all copyright
#    notices that are currently in place, and this list of conditions without
#    modification.
# 
# 2. All redistributions in binary form must retain all occurrences of the
#    above copyright notice and web site addresses that are currently in
#    place (for example, in the About boxes).
# 
# 3. The origin of this software must not be misrepresented; you must not
#    claim that you wrote the original software. If you use this software to
#    distribute a product, an acknowledgment in the product documentation
#    would be appreciated but is not required.
# 
# 4. Modified versions in source or binary form must be plainly marked as
#    such, and must not be misrepresented as being the original software.
#    
#    ::::::::::::::: www.blogging-it.com :::::::::::::::
#===================================================================


#===================================================================
#	SETTINGS
#===================================================================
BASE_DIR="$(dirname $0)"
SCRIPT_DIR="$( cd -P -- "$(dirname -- "$(command -v -- "$0")")" && pwd -P )"
WORK_DIR="$SCRIPT_DIR/.."
WAR_APP_PATH="$WORK_DIR/module-application/target/angular2-qickstart-javaee7-application.war"
WAR_CLIENT_PATH="$WORK_DIR/module-client-web/target/angular2-qickstart-javaee7-client-web.war"

DOCKER_VERSION="1.0.0"
DOCKER_EXEC="docker"
DOCKER_IMAGE="angular2/javaee7-quickstart"
DOCKER_NAME="angular2-javaee7-quickstart"
DOCKER_PORT="8080:8080"

ACTION="$1"

#===================================================================
#	FUNCTIONS
#===================================================================

function isAppInstalled {
  local isInst_=1
  type $1 >/dev/null 2>&1 || { local isInst_=0; }  # set to 0 if not found
  echo "$isInst_"
}


#===================================================================
#	MAIN
#===================================================================

valid=true

if [[ $(isAppInstalled docker) != 1 ]]; then
    echo "ERROR: Please install 'docker' to execute this script"
    valid=false
fi

if [ ! -f "$WAR_APP_PATH" ]; then
    echo "ERROR: file '$WAR_APP_PATH' missing! Please execute maven build..."
    valid=false
fi

if [ ! -f "$WAR_CLIENT_PATH" ]; then
    echo "ERROR: file '$WAR_CLIENT_PATH' missing! Please execute maven build..."
    valid=false
fi

if [ "$valid" = false ] ; then
    exit 1
fi


EXEC_CMD=""
case "$ACTION" in
        build)
        	EXEC_CMD="$DOCKER_EXEC build -t $DOCKER_IMAGE:$DOCKER_VERSION ."
            ;;

        start)
        	EXEC_CMD="$DOCKER_EXEC run -P -it --rm -p $DOCKER_PORT --name $DOCKER_NAME $DOCKER_IMAGE:$DOCKER_VERSION"
            ;;
            
        startbg)
        	EXEC_CMD="$DOCKER_EXEC run -P -d -it --rm -p $DOCKER_PORT --name $DOCKER_NAME $DOCKER_IMAGE:$DOCKER_VERSION"
            ;;            
            
        stop)
        	EXEC_CMD="$DOCKER_EXEC stop $DOCKER_NAME"
            ;;
            
        remove)
        	EXEC_CMD="$DOCKER_EXEC rm -f $DOCKER_NAME ; docker rmi -f $DOCKER_IMAGE:$DOCKER_VERSION"
            ;;                             

        login)
        	EXEC_CMD="$DOCKER_EXEC exec -i -t $DOCKER_NAME /bin/bash"
            ;; 
                                                
        *)
            echo $"Usage: $ACTION {build|start|startbg|stop|remove|login}"
            exit 1
esac

cd "$WORK_DIR"

echo "Run '$EXEC_CMD'"
eval $EXEC_CMD


exit 0