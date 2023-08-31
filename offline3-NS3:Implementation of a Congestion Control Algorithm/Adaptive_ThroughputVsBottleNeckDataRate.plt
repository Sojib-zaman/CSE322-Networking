
set terminal pngcairo enhanced font "arial,12" size 800,600
set output "Adaptive_ThroughputVsBottleNeckDataRate.png"
set title "Throughput vs. Bottleneck Data Rate"
set ylabel "Throughput (Mbps)"
set xlabel "Bottleneck Data Rate (Mbps)"


set key left top


plot "Task1_output/Adaptive_output1.dat" using 1:3 with linespoints lw 2 lc rgb "blue" title "TcpNewReno", \
 "Task1_output/Adaptive_output1.dat" using 1:4 with linespoints lw 2 lc rgb "red" title "TcpAdaptiveReno"



