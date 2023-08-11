set terminal png
set output "MobilePacketRateVsRatio.png"
set title "PacketRateVsRatio"
set xlabel "PacketRate"
set ylabel "Ratio"
plot "Mobile_offline_output/packet.dat" using 3:6 with lines title "PacketRate/Ratio"
