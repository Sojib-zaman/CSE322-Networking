#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/ssid.h"
#include "ns3/yans-wifi-helper.h"


#define tx_range 5
#define packet_size 1024
// Default Network Topology
//
//   Wifi 10.1.3.0                  Wifi 10.1.2.0
//                 AP
//  *    *    *    *                *    *     *    *
//  |    |    |    |    10.1.1.0    |    |     |    |
// n5   n6   n7   n0 -------------- n1   n2   n3   n4
//                   point-to-point

using namespace ns3 ;
NS_LOG_COMPONENT_DEFINE("StaticOffline") ;

uint64_t Rx_Packets = 0 ;
uint64_t Tx_Packets = 0 ;
uint64_t Rx_bits = 0 ;
uint64_t Tx_bits = 0 ;
uint64_t lastTotalRx=0;
double_t Packet_delivery_ratio = 0.0 ;
double_t Network_Throughput = 0.0 ;

void
SenderTrace(ns3::Ptr<const ns3::Packet> packet_ptr)
{

    ++Tx_Packets ;
    Tx_bits += packet_ptr->GetSize()*8 ;
}


void
ReceiverTrace(ns3::Ptr<const ns3::Packet> packet_ptr , const ns3::Address &address)
{

    ++Rx_Packets ;
    Rx_bits += packet_ptr->GetSize()*8 ;
}

void calculate_metrics()
{
    if(Rx_bits!=0 && Tx_bits!=0)
    {
        uint64_t time = Simulator::Now().GetMilliSeconds();
        Network_Throughput = (double_t)Rx_bits/time ;
        Packet_delivery_ratio = (double_t) Rx_bits/Tx_bits ;

   // std::cout<<Rx_bits<<" "<<lastTotalRx<<std::endl ;
    }
    //Simulator::Schedule(MilliSeconds(500), &calculate_metrics);
}



int main(int argc , char* argv[])
{
    CommandLine cmd(__FILE__) ;
    uint32_t n_nodes=20 ;
    uint32_t n_flow=50 ;
    uint32_t n_packets=100 ;
    uint32_t conv_area = 1;
    bool tracing = true ;
    bool verbose = false ;


    cmd.AddValue("verbose","Whether echo packets will log or not",verbose) ;
    cmd.AddValue("tracing","Whether to trace pcap files" , tracing) ;


    cmd.AddValue("n_nodes","total number of nodes",n_nodes) ;
    cmd.AddValue("n_flow","Number of packets sent from the sender nodes" , n_flow) ;
    cmd.AddValue("conv_area","converage area" , conv_area) ;
    cmd.AddValue("n_packets","Packet count",n_packets) ;




    cmd.Parse(argc,argv) ;

    uint32_t nwifi = n_nodes/2 ;

    //............................................
    Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(packet_size));
    //...........................................





    Time::SetResolution(Time::NS) ;

    if(verbose)
    {
    // Enable logging for OnOffApplication (sender)
   LogComponentEnable("OnOffApplication", LOG_LEVEL_INFO);

    // Enable logging for PacketSink (receiver)
   LogComponentEnable("PacketSink", LOG_LEVEL_INFO);
    }

// ------------------------------------
    NodeContainer XY_p2pnodes ;
    XY_p2pnodes.Create(2) ;

    //* Setting up the sender nodes
    NodeContainer X_nS_st_nodes ;
    X_nS_st_nodes.Create(nwifi) ;
    NodeContainer X_ap = XY_p2pnodes.Get(0) ;

    //* Setting up the receiver nodes
    NodeContainer Y_nR_st_nodes ;
    Y_nR_st_nodes.Create(nwifi) ;
    NodeContainer Y_ap = XY_p2pnodes.Get(1) ;

// -------------------------------------


//*****************************************

    PointToPointHelper XY_p2pHelper;
    XY_p2pHelper.SetDeviceAttribute ("DataRate",
    StringValue ("5Mbps"));
    XY_p2pHelper.SetChannelAttribute ("Delay",
    StringValue ("2ms"));


    NetDeviceContainer XY_p2pnetdevices ;
    XY_p2pnetdevices = XY_p2pHelper.Install(XY_p2pnodes) ;


    YansWifiChannelHelper channel = YansWifiChannelHelper::Default() ;
    channel.AddPropagationLoss("ns3::RangePropagationLossModel", "MaxRange", DoubleValue(conv_area*tx_range));


    YansWifiPhyHelper Sender_phy ;
    Sender_phy.SetChannel(channel.Create()) ;
    YansWifiPhyHelper Receiver_phy ;
    Receiver_phy.SetChannel(channel.Create()) ;


    WifiMacHelper mac ;
    Ssid Sender_ssid = Ssid("Sender") ;
    Ssid Receiver_ssid = Ssid("Receiver") ;

    WifiHelper wifi ;

    NetDeviceContainer Sender_nS_netdevices ;
    mac.SetType("ns3::StaWifiMac","Ssid",SsidValue(Sender_ssid),"ActiveProbing",BooleanValue(false)) ;
    Sender_nS_netdevices = wifi.Install(Sender_phy,mac,X_nS_st_nodes) ;


    NetDeviceContainer Sender_X_netdevice ;
    mac.SetType("ns3::ApWifiMac","Ssid",SsidValue(Sender_ssid)) ;
    Sender_X_netdevice = wifi.Install(Sender_phy,mac,X_ap) ;


    NetDeviceContainer Receiver_nS_netdevices ;
    mac.SetType("ns3::StaWifiMac","Ssid",SsidValue(Receiver_ssid),"ActiveProbing",BooleanValue(false)) ;
    Receiver_nS_netdevices = wifi.Install(Receiver_phy,mac,Y_nR_st_nodes) ;


    NetDeviceContainer Receiver_Y_netdevice ;
    mac.SetType("ns3::ApWifiMac","Ssid",SsidValue(Receiver_ssid)) ;
    Receiver_Y_netdevice = wifi.Install(Receiver_phy,mac,Y_ap) ;



//*****************************************

//-----------------------------------------


MobilityHelper Sender_mobility;

//! Can be done in two ways


Sender_mobility.SetPositionAllocator("ns3::RandomRectanglePositionAllocator",
    "X",
    StringValue("ns3::UniformRandomVariable[Min=0.0|Max=" + std::to_string(sqrt(n_nodes)) + "]"),
    "Y",
    StringValue("ns3::UniformRandomVariable[Min=0.0|Max=" + std::to_string(sqrt(n_nodes)) + "]"));



//Sender_mobility.SetPositionAllocator("ns3::RandomDiscPositionAllocator", "Rho", ns3::StringValue("ns3::UniformRandomVariable[Min=0.0|Max=" + std::to_string(conv_area*2) + "]"));


Sender_mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
Sender_mobility.Install(X_nS_st_nodes);
Sender_mobility.Install(X_ap);


MobilityHelper Receiver_mobility ;


//! Can be done in two ways
// can be used any value , graph may change
Receiver_mobility.SetPositionAllocator("ns3::RandomRectanglePositionAllocator",
    "X",
    StringValue("ns3::UniformRandomVariable[Min=0.0|Max=" + std::to_string(sqrt(n_nodes)) + "]"),
    "Y",
    StringValue("ns3::UniformRandomVariable[Min=0.0|Max=" + std::to_string(sqrt(n_nodes)) + "]"));

//Receiver_mobility.SetPositionAllocator("ns3::RandomDiscPositionAllocator", "Rho", ns3::StringValue("ns3::UniformRandomVariable[Min=0.0|Max=" + std::to_string(conv_area*2) + "]"));



Receiver_mobility.SetMobilityModel("ns3::ConstantPositionMobilityModel");
Receiver_mobility.Install(Y_nR_st_nodes);
Receiver_mobility.Install(Y_ap);

//------------------------------------------

//******************************************

    InternetStackHelper stack ;
    stack.Install(XY_p2pnodes) ;
    stack.Install(X_nS_st_nodes) ;
    stack.Install(Y_nR_st_nodes) ;



    Ipv4AddressHelper ip ;
    ip.SetBase("10.1.1.0","255.255.255.0") ;
    Ipv4InterfaceContainer XY_p2p_Interface = ip.Assign(XY_p2pnetdevices) ;

    ip.SetBase("10.1.2.0","255.255.255.0") ;
    Ipv4InterfaceContainer Receiver_stat_wifi = ip.Assign(Receiver_nS_netdevices) ;
    Ipv4InterfaceContainer Receiver_AP_wifi = ip.Assign(Receiver_Y_netdevice) ;


    ip.SetBase("10.1.3.0","255.255.255.0") ;
    Ipv4InterfaceContainer Sender_stat_wifi= ip.Assign(Sender_nS_netdevices) ;
    Ipv4InterfaceContainer Sender_AP_wifi = ip.Assign(Sender_X_netdevice) ;



//******************************************


//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    Ipv4GlobalRoutingHelper::PopulateRoutingTables() ;
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>


//.....................................................
// Packetsink Receiver
ApplicationContainer senderAppContainer ;
ApplicationContainer Receiever_app_container ;

for(uint32_t i = 0 ; i<nwifi ; i++)
{
    PacketSinkHelper Receiver_Helper("ns3::TcpSocketFactory",InetSocketAddress(Ipv4Address::GetAny(), 9));
    Receiever_app_container.Add( Receiver_Helper.Install(Y_nR_st_nodes.Get(i)))  ;
}

//OnOff Sender

for(uint32_t i = 0 ; i<n_flow ; i++)
{
OnOffHelper sender_helper("ns3::TcpSocketFactory",InetSocketAddress(Receiver_stat_wifi.GetAddress(i%nwifi), 9));
sender_helper.SetAttribute("PacketSize", UintegerValue(packet_size));
sender_helper.SetAttribute("DataRate", DataRateValue(DataRate(packet_size*n_packets*8)));
senderAppContainer.Add(sender_helper.Install(X_nS_st_nodes.Get(i%nwifi))) ;
}




//.....................................................

//+++++++++++++++++++++++++++++++++++++++++++++++++++++

for(uint32_t i = 0 ; i<senderAppContainer.GetN() ; i++)
    senderAppContainer.Get(i)->TraceConnectWithoutContext("Tx",MakeCallback(&SenderTrace)) ;

for(uint32_t i = 0 ; i<Receiever_app_container.GetN() ; i++)
    Receiever_app_container.Get(i)->TraceConnectWithoutContext("Rx",MakeCallback(&ReceiverTrace)) ;


//+++++++++++++++++++++++++++++++++++++++++++++++++++++











//-----------------------------------------------------
Receiever_app_container.Start(Seconds(0.0)) ;
senderAppContainer.Start(Seconds(1.0)) ;






Simulator::Stop(Seconds(10.0)) ;
Simulator::Schedule(Seconds(9.9),calculate_metrics);
Simulator::Run() ;
Simulator::Destroy() ;

std::cout<<n_nodes ;
std::cout << " "<<n_flow;
std::cout << " "<<n_packets;
std::cout << " "<<conv_area;

std::cout << " " << Network_Throughput ;
std::cout << " " << Packet_delivery_ratio<<std::endl;

//-----------------------------------------------------













}
