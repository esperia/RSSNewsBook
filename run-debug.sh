script_dir="$(cd "$(dirname "${BASH_SOURCE:-${(%):-%N}}")"; pwd)"
echo $script_dir

version=$1
if [ ! -f $script_dir/libs/spigot-${version}.jar ]; then
  echo "Cannot find the version jar: $version"
  exit 1
fi

cd $script_dir/
./gradlew jar -PpluginVersion="${version}"

# Run server
mkdir -p server-${version}
pushd server-${version}
java -Xdebug -Xnoagent -Djava.compiler=NONE \
  -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 \
  -Dfile.encoding=UTF-8 \
  -jar $script_dir/libs/spigot-${version}.jar
popd

