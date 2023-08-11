set terminal png
set output "MobilenodeVsThroughput.png"
set title "nodeVsThroughput"
set xlabel "Number of nodes"
set ylabel "Throughput"
plot "Mobile_offline_output/node.dat" using 1:5 with lines title "Node/Throughput"
