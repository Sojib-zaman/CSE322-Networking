

set terminal pngcairo enhanced font "arial,12" size 800,600
set output "Adaptive_FairNessIndexVsBottleNeckDataRate.png"
set title "FairNessIndex vs. Bottleneck Data Rate"
set ylabel "FairNessIndex"
set xlabel "Bottleneck Data Rate (Mbps)"


set key font "Times New Roman,12"
set key right top
set grid linecolor rgb "gray"


plot "Task1_output/Adaptive_output1.dat" using 1:5 with linespoints lw 1 lc rgb "blue" title "JainIndex"
