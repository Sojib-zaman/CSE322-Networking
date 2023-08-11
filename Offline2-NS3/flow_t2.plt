set terminal png
set output "MobileFlowVsThroughput.png"
set title "FlowVsThroughput"
set xlabel "Flow"
set ylabel "Throughput"
plot "Mobile_offline_output/flow.dat" using 2:5 with lines title "Flow/Throughput"
