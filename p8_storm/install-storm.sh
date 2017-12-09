CURR_DIR=`pwd`
mkdir -p ~/Downloads/storm-setup
cd ~/Downloads/storm-setup/
wget http://www-us.apache.org/dist/storm/apache-storm-1.0.5/apache-storm-1.0.5.tar.gz
tar zxvf apache-storm-1.0.5.tar.gz
echo "
storm.zookeeper.servers:

     - \"localhost\"

storm.zookeeper.port: 2181

nimbus.host: \"localhost\"

storm.local.dir: \"/mnt/storm\"

java.library.path: \"/usr/local/lib\"

supervisor.slots.ports:

     - 6700

     - 6701

     - 6702

     - 6703

worker.childopts: \"-Xmx768m\"

nimbus.childopts: \"-Xmx512m\"

supervisor.childopts: \"-Xmx256m\"
" >> apache-storm-1.0.5/conf/storm.yaml
mv apache-storm-1.0.5 /opt/
cd $CURR_DIR