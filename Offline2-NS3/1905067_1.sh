#!/usr/bin/bash
NODES=(20 40 60 80 100)
FLOWS=(10 20 30 40 50)
PACKETS_PER_SECOND=(100 200 300 400 500)
COVERAGE_FACTORS=(1 2 3 4 5)


rm -r "Static_offline_output"
mkdir "Static_offline_output"

node_file="Static_offline_output/node.dat"
coverage_file="Static_offline_output/coverage.dat"
flow_file="Static_offline_output/flow.dat"
packet_file="Static_offline_output/packet.dat"


for node in ${NODES[@]}
do
    changed_flow=$((node/2))
    result=$(./ns3 run "scratch/1905067_1 --n_nodes=$node --n_flow=$changed_flow")
    echo "$result" >> "$node_file"
done

echo "node_count file done"
for cna in ${COVERAGE_FACTORS[@]}
do

    result=$(./ns3 run "scratch/1905067_1 --conv_area=$cna")
    echo "$result" >> "$coverage_file"
done
echo "cov_area file done"
for pk in ${PACKETS_PER_SECOND[@]}
do

    result=$(./ns3 run "scratch/1905067_1 --n_packets=$pk")
    echo "$result" >> "$packet_file"
done
echo "packet_count file done"
for flow in ${FLOWS[@]}
do

    result=$(./ns3 run "scratch/1905067_1 --n_flow=$flow")
    echo "$result" >> "$flow_file"
done
echo "packet_count file done"


nodeT_plt="node_tt.plt"
gnuplot "$nodeT_plt"
nodeR_plt="node_rr.plt"
gnuplot "$nodeR_plt"
echo "Graph : Node_Count done"
conT_plt="con_t.plt"
gnuplot "$conT_plt"
conR_plt="con_r.plt"
gnuplot "$conR_plt"
echo "Graph : cov_area done"
pkT_plt="pk_t.plt"
gnuplot "$pkT_plt"
pkR_plt="pk_r.plt"
gnuplot "$pkR_plt"
echo "Graph : Packet_Count done"
flowT_plt="flow_t.plt"
gnuplot "$flowT_plt"
flowR_plt="flow_r.plt"
gnuplot "$flowR_plt"
echo "Graph : Flow done"
