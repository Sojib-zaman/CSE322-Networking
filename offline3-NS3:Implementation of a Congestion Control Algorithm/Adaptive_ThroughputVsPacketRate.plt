
set terminal pngcairo enhanced font "arial,12" size 800,600
set output "Adaptive_ThroughputVsPacketRate.png"


set title "Throughput vs. PacketRate Data Rate"
set ylabel "Throughput (Mbps)"
set xlabel "PacketRate Data Rate "
set autoscale y
set autoscale x

set key left top


plot "Task1_output/Adaptive_output2.dat" using 2:3 with linespoints lw 2 lc rgb "blue" title "TcpNewReno", \
"Task1_output/Adaptive_output2.dat" using 2:4 with linespoints lw 2 lc rgb "red" title "TcpAdaptiveReno"




