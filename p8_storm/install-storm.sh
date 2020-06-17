CURR_DIR=`pwd`
mkdir -p ~/Downloads/storm-setup
cd ~/Downloads/storm-setup/
wget http://www-us.apache.org/dist/storm/apache-storm-1.2.3/apache-storm-1.2.3.tar.gz
tar zxvf apache-storm-1.2.3.tar.gz
echo "
storm.zookeeper.servers:
     - \"itd353-ext\"

storm.zookeeper.port: 2181

nimbus.host: \"itd353-ext\"

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

nimbus.seeds: [\"itd353-ext\"]

storm.zookeeper.root: \"/storm03\"

" >> apache-storm-1.2.3/conf/storm.yaml
mv apache-storm-1.2.3 /opt/
chown itd353 -R /opt/apache-storm-1.2.3
rm -rf /mnt/storm/*
cd $CURR_DIR

