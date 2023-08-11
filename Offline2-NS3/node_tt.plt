set terminal png
set output "STATIC_nodeVsThroughput.png"
set title "nodeVsThroughput"
set xlabel "Number of nodes"
set ylabel "Throughput"
plot "Static_offline_output/node.dat" using 1:5 with lines title "Node/Throughput"
