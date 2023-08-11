set terminal png
set output "STATIC_PacketRateVsRatio.png"
set title "PacketRateVsRatio"
set xlabel "PacketRate"
set ylabel "Ratio"
plot "Static_offline_output/packet.dat" using 3:6 with lines title "PacketRate/Ratio"
