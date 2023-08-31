
set terminal pngcairo enhanced font "arial,12" size 800,600
set output "Adaptive_FairNessIndexVsPacketRate.png"
set title "FairNessIndex vs. PacketRate"
set ylabel "FairNessIndex"
set xlabel "PacketRate"


set key font "Times New Roman,12"
set key right top
set grid linecolor rgb "gray"


plot "Task1_output/Adaptive_output2.dat" using 2:5 with linespoints lw 1 lc rgb "green" title "JainIndex"
