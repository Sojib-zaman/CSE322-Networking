# Set terminal and output
set terminal pngcairo enhanced font "arial,12" size 800,600
set output "ThroughputVsPacketRate.png"

# Set graph title and labels
set title "Throughput vs. PacketRate Data Rate"
set ylabel "Throughput (Mbps)"
set xlabel "PacketRate Data Rate"
set autoscale y
set autoscale x
# Customize the legend
set key left top

# Customize the plot style with lines and points
plot "Task1_output/output2.dat" using 2:3 with linespoints lw 2 lc rgb "blue" title "TcpNewReno", \
"Task1_output/output2.dat" using 2:4 with linespoints lw 2 lc rgb "red" title "TcpWestWoodPlus"