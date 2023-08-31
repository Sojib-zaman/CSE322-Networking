
set terminal pngcairo enhanced font "arial,12" size 800,600
set output "HighSpeed_ThroughputVsPacketRate.png"


set title "Throughput vs. PacketRate Data Rate"
set ylabel "Throughput (Mbps)"
set xlabel "PacketRate Data Rate"
set autoscale y
set autoscale x

set key font "Times New Roman,12"
set key right top
set grid linecolor rgb "gray"


plot "Task1_output/HighSpeed_output2.dat" using 2:3 with linespoints lw 1 lc rgb "blue" title "TcpNewReno", \
"Task1_output/HighSpeed_output2.dat" using 2:4 with linespoints lw 1 lc rgb "red" title "TcpHighSpeed"



