set terminal pngcairo enhanced font "arial,12" size 800,600
set output "HighSpeed_CongestionWindowVsTime.png"

set title "CongestionWindow vs. Time"
set ylabel "CongestionWindow"
set xlabel "Time"


set key font "Times New Roman,12"
set key right top
set grid linecolor rgb "gray"

plot "renocwd.txt" using 1:2 with lines lw 1 lc rgb "blue" title "TcpNewReno", \
 "othercwnd.txt" using 1:2 with lines lw 1 lc rgb "green" title "TcpHighSpeed"
