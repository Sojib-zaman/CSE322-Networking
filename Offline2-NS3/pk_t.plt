set terminal png
set output "STATIC_PacketRateVsThroughput.png"
set title "PacketRateVsThroughput"
set xlabel "PacketRate"
set ylabel "Throughput"
plot "Static_offline_output/packet.dat" using 3:5 with lines title "PacketRate/Throughput"
