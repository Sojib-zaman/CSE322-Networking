set terminal png
set output "MobileSpeedVsThroughput.png"
set title "SpeedVsThroughput"
set xlabel "Speed"
set ylabel "Throughput"
plot "Mobile_offline_output/speed.dat" using 4:5 with lines title "Speed/Throughput"
