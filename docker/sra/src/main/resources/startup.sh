#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

cd /opt/syncope/conf

sed "s/\${CORE_SCHEME}/$CORE_SCHEME/" sra.properties.template | 
sed "s/\${CORE_HOST}/$CORE_HOST/" | sed "s/\${CORE_PORT}/$CORE_PORT/" > sra.properties

export LOADER_PATH="/opt/syncope/conf,/opt/syncope/lib"
java -Dfile.encoding=UTF-8 -server -Xms1536m -Xmx1536m -XX:NewSize=256m -XX:MaxNewSize=256m \
 -XX:+DisableExplicitGC -Djava.security.egd=file:/dev/./urandom -jar /opt/syncope/lib/syncope-sra.jar