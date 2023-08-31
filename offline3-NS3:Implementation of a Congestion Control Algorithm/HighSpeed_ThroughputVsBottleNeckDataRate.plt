
set terminal pngcairo enhanced font "arial,12" size 800,600
set output "HighSpeed_ThroughputVsBottleNeckDataRate.png"
set title "Throughput vs. Bottleneck Data Rate"
set ylabel "Throughput (Mbps)"
set xlabel "Bottleneck Data Rate (Mbps)"


set key font "Times New Roman,12"
set key right top
set grid linecolor rgb "gray"


plot "Task1_output/HighSpeed_output1.dat" using 1:3 with linespoints lw 1 lc rgb "blue" title "TcpNewReno", \
 "Task1_output/HighSpeed_output1.dat" using 1:4 with linespoints lw 1 lc rgb "red" title "TcpHighSpeed"


