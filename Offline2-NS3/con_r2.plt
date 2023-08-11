set terminal png
set output "MobileSpeedVsRatio.png"
set title "SpeedVsRatio"
set xlabel "Speed"
set ylabel "Ratio"
plot "Mobile_offline_output/speed.dat" using 4:6 with lines title "Speed/Ratio"
