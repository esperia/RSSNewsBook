script_dir="$(cd "$(dirname "${BASH_SOURCE:-${(%):-%N}}")"; pwd)"

cd $script_dir/..
./gradlew jar

cd $script_dir
java -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Dfile.encoding=UTF-8 -jar $script_dir/../libs/spigot-1.9.2.jar

