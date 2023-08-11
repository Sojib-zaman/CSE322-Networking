set terminal png
set output "STATIC_FlowVsThroughput.png"
set title "FlowVsThroughput"
set xlabel "Flow"
set ylabel "Throughput"
plot "Static_offline_output/flow.dat" using 2:5 with lines title "Flow/Throughput"
