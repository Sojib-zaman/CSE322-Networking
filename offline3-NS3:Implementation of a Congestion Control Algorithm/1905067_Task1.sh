
#!/usr/bin/bash



rm -r "Task1_output"
mkdir "Task1_output"

outputfile1="Task1_output/output1.dat"
outputfile2="Task1_output/output2.dat"

Adaptive_outputfile1="Task1_output/Adaptive_output1.dat"
Adaptive_outputfile2="Task1_output/Adaptive_output2.dat"

HighSpeed_outputfile1="Task1_output/HighSpeed_output1.dat"
HighSpeed_outputfile2="Task1_output/HighSpeed_output2.dat"


loss_rates="0.01 0.001 0.0001 0.00001 0.000001"
data_rates="1 10 20 50 100 200 300"

# Changing data rate and running with TCP-NEWRENO+TcpWestwoodPlus


for data_rate in ${data_rates[@]}; do
    
    result=$(./ns3 run "scratch/1905067_offline --int_bottleneck_datarate=$data_rate")
    echo "$result" >> "$outputfile1" 
done




# Changing PACKET LOSS and running with TCP-NEWRENO+TcpWestwoodPlus

dr=50
for rate in ${loss_rates[@]};
do
   
    result=$(./ns3 run "scratch/1905067_offline --packet_loss=$rate --int_bottleneck_datarate=$dr")
    echo "$result" >> "$outputfile2" 
done


# graph plot with newReno+westwoodplus
cwnd_plt="CongestionVsTime.plt"
gnuplot "$cwnd_plt"







# Changing data rate and running with TCP-NEWRENO+TcpAdaptiveReno

for data_rate in ${data_rates[@]}; do
   
    result=$(./ns3 run "scratch/1905067_offline --algo2="ns3::TcpAdaptiveReno" --int_bottleneck_datarate=$data_rate")
    echo "$result" >> "$Adaptive_outputfile1" 
    
done



# Changing PACKET_LOSS and running with TCP-NEWRENO+TcpAdaptiveReno
dr=50
for rate in ${loss_rates[@]};
do
    
    result=$(./ns3 run "scratch/1905067_offline --algo2="ns3::TcpAdaptiveReno" --packet_loss=$rate --int_bottleneck_datarate=$dr" )
    echo "$result" >> "$Adaptive_outputfile2" 
done


Adaptive_jain="Task1_output/Adaptive_jain.dat"




 
cwnd_plt="Adaptive_JindexVsThrougput.plt"
gnuplot "$cwnd_plt"
cwnd_plt="Adaptive_JindexVsPacket.plt"
gnuplot "$cwnd_plt"


# Changing DATA_RATE and running with TCP-NEWRENO+TcpHighSpeed
for data_rate in ${data_rates[@]}; do
    
    result=$(./ns3 run "scratch/1905067_offline --algo2="ns3::TcpHighSpeed" --int_bottleneck_datarate=$data_rate")
    echo "$result" >> "$HighSpeed_outputfile1" 
done


# Changing PACKET_LOSS and running with TCP-NEWRENO+TcpHighSpeed
dr=50
for rate in ${loss_rates[@]};
do
    
    result=$(./ns3 run "scratch/1905067_offline --algo2="ns3::TcpHighSpeed" --packet_loss=$rate --int_bottleneck_datarate=$dr" )
    echo "$result" >> "$HighSpeed_outputfile2" 
done


cwnd_plt="Highspeed_JindexVsThrougput.plt"
gnuplot "$cwnd_plt"
cwnd_plt="Highspeed_JindexVsPacket.plt"
gnuplot "$cwnd_plt"


nodeT_plt="ThroughputVsBottleNeckDataRate.plt"
gnuplot "$nodeT_plt"
nodeR_plt="ThroughputVsPacketRate.plt"
gnuplot "$nodeR_plt"

nodeT_plt="Adaptive_ThroughputVsBottleNeckDataRate.plt"
gnuplot "$nodeT_plt"
nodeR_plt="Adaptive_ThroughputVsPacketRate.plt"
gnuplot "$nodeR_plt"




# running with TCP-NEWRENO+TcpHighSpeed
result=$(./ns3 run "scratch/1905067_offline --algo2="ns3::TcpAdaptiveReno"")
cwnd_plt="Adaptive_CongestionVsTime.plt"
gnuplot "$cwnd_plt"

nodeT_plt="HighSpeed_ThroughputVsBottleNeckDataRate.plt"
gnuplot "$nodeT_plt"
nodeR_plt="HighSpeed_ThroughputVsPacketRate.plt"
gnuplot "$nodeR_plt"

result=$(./ns3 run "scratch/1905067_offline --algo2="ns3::TcpHighSpeed"")
cwnd_plt="HighSpeed_CongestionVsTime.plt"
gnuplot "$cwnd_plt"