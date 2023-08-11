set terminal png
set output "MobilePacketRateVsThroughput.png"
set title "PacketRateVsThroughput"
set xlabel "PacketRate"
set ylabel "Throughput"
plot "Mobile_offline_output/packet.dat" using 3:5 with lines title "PacketRate/Throughput"
