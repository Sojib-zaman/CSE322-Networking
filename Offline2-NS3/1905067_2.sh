#!/usr/bin/bash
NODES=(20 40 60 80 100)
FLOWS=(10 20 30 40 50)
PACKETS_PER_SECOND=(100 200 300 400 500)
SPEED=(5 10 15 20 25)


rm -r "Mobile_offline_output"
mkdir "Mobile_offline_output"

node_file="Mobile_offline_output/node.dat"
speed_file="Mobile_offline_output/speed.dat"
flow_file="Mobile_offline_output/flow.dat"
packet_file="Mobile_offline_output/packet.dat"


for node in ${NODES[@]}
do

    changed_flow=$((node/2))
    result=$(./ns3 run "scratch/1905067_2 --n_nodes=$node --n_flow=$changed_flow")
    echo "$result" >> "$node_file"
done

echo "node_count file done"
for cna in ${SPEED[@]}
do

    result=$(./ns3 run "scratch/1905067_2 --speed=$cna")
    echo "$result" >> "$speed_file"
done
echo "speed file done"
for pk in ${PACKETS_PER_SECOND[@]}
do

    result=$(./ns3 run "scratch/1905067_2 --n_packets=$pk")
    echo "$result" >> "$packet_file"
done
echo "packet_count file done"
for flow in ${FLOWS[@]}
do

    result=$(./ns3 run "scratch/1905067_2 --n_flow=$flow")
    echo "$result" >> "$flow_file"
done

echo "packet_count file done"

nodeT_plt="node_tt2.plt"
gnuplot "$nodeT_plt"
nodeR_plt="node_rr2.plt"
gnuplot "$nodeR_plt"

echo "Graph : Node_Count done"

conT_plt="con_t2.plt"
gnuplot "$conT_plt"
conR_plt="con_r2.plt"
gnuplot "$conR_plt"

echo "Graph : Speed done"

pkT_plt="pk_t2.plt"
gnuplot "$pkT_plt"
pkR_plt="pk_r2.plt"
gnuplot "$pkR_plt"

echo "Graph : Packet_Count done"


flowT_plt="flow_t2.plt"
gnuplot "$flowT_plt"
flowR_plt="flow_r2.plt"
gnuplot "$flowR_plt"


echo "Graph : Flow done"
