#!/bin/bash

cd /workspace

# 检查jar包是否存在
if [ ! -f "ruoyi-admin/target/ruoyi-admin.jar" ]; then
    echo "Jar包不存在，首次编译中..."
    mvn -pl ruoyi-admin -am -DskipTests install
    echo "首次编译完成"
fi

# 直接启动jar包，支持热重载
cd ruoyi-admin
exec java $JAVA_OPTS \
    -Dspring.devtools.restart.enabled=true \
    -Dspring.devtools.livereload.enabled=true \
    -Dspring-boot.run.profiles=$SPRING_PROFILES_ACTIVE \
    -jar target/ruoyi-admin.jar
