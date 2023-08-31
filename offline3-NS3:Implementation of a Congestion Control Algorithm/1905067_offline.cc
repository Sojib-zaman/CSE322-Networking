
#include "1905067_offline.h"
#include "ns3/applications-module.h"
#include "ns3/core-module.h"
#include "ns3/csma-module.h"
#include "ns3/internet-module.h"
#include "ns3/mobility-module.h"
#include "ns3/network-module.h"
#include "ns3/point-to-point-module.h"
#include "ns3/point-to-point-dumbbell.h"
#include "ns3/flow-monitor-module.h"
#include <cmath>
#include <fstream>

#define tx_range 5
#define packet_size 1024
#define total_time 60


using namespace ns3 ;
using namespace std ; 
NS_LOG_COMPONENT_DEFINE("OfflineTask1") ;
std::ofstream RenoCwnd;  
std::ofstream OtherCwnd;
TutorialApp::TutorialApp()
    : m_socket(nullptr),
      m_peer(),
      m_packetSize(0),
      m_nPackets(0),
      m_dataRate(0),
      m_sendEvent(),
      m_running(false),
      m_packetsSent(0)
{
}

TutorialApp::~TutorialApp()
{
    m_socket = nullptr;
}

/* static */
TypeId
TutorialApp::GetTypeId()
{
    static TypeId tid = TypeId("TutorialApp")
                            .SetParent<Application>()
                            .SetGroupName("Tutorial")
                            .AddConstructor<TutorialApp>();
    return tid;
}

void
TutorialApp::Setup(Ptr<Socket> socket,
                   Address address,
                   uint32_t packetSize,
                   uint32_t nPackets,
                   DataRate dataRate)
{
    m_socket = socket;
    m_peer = address;
    m_packetSize = packetSize;
    m_nPackets = nPackets;
    m_dataRate = dataRate;
}

void
TutorialApp::StartApplication()
{
    m_running = true;
    m_packetsSent = 0;
    m_socket->Bind();
    m_socket->Connect(m_peer);
    SendPacket();
}

void
TutorialApp::StopApplication()
{
    m_running = false;

    if (m_sendEvent.IsRunning())
    {
        Simulator::Cancel(m_sendEvent);
    }

    if (m_socket)
    {
        m_socket->Close();
    }
}

void
TutorialApp::SendPacket()
{
    Ptr<Packet> packet = Create<Packet>(m_packetSize);
    m_socket->Send(packet);

    if (++m_packetsSent < m_nPackets)
    {
        ScheduleTx();
    }
}

void
TutorialApp::ScheduleTx()
{
    if (m_running)
    {
        Time tNext(Seconds(m_packetSize * 8 / static_cast<double>(m_dataRate.GetBitRate())));
        m_sendEvent = Simulator::Schedule(tNext, &TutorialApp::SendPacket, this);
    }
}

static void
CwndChange(uint32_t oldCwnd, uint32_t newCwnd)
{
   //NS_LOG_UNCOND(Simulator::Now().GetSeconds() << "\t" << newCwnd);
  
   RenoCwnd<<Simulator::Now().GetSeconds()<<" "<<newCwnd<<endl ; 

}


static void
CwndChange2(uint32_t oldCwnd, uint32_t newCwnd)
{
   // NS_LOG_UNCOND(Simulator::Now().GetSeconds() << "\t" << newCwnd);
   OtherCwnd<<Simulator::Now().GetSeconds()<<" "<<newCwnd<<endl ; 

}

#define n_leaf_nodes 4 
#define n_flow 2 
#define left_right_nodes 2 



int main(int argc , char* argv[])
{
    



    RenoCwnd.open("renocwd.txt") ; 
    OtherCwnd.open("othercwnd.txt") ; 



 


        if (!RenoCwnd.is_open()) {
        std::cerr << "Error opening files for writing!" << std::endl;
        return 1;
    }
            if (!OtherCwnd.is_open()) {
        std::cerr << "Error opening files for writing 2!" << std::endl;
        return 1;
    }

    CommandLine cmd(__FILE__) ;

    // total number of leaf nodes 

    double packet_loss = 1.0 / pow(10, 6);  


    uint32_t  int_bottleneck_delay = 100 ;
    string  bottleneck_delay = "100ms" ; 

    //+ uint32_t  int_normal_delay=1 ; 
    string normal_delay="1ms" ; 

    uint32_t  int_bottleneck_datarate = 1 ;
    

    //? uint32_t  send_receive_int_data_rate = 1000 ; 
    string data_rate = "1Gbps" ;






    string algo1 = "ns3::TcpNewReno" ; 
    string algo2 = "ns3::TcpWestwoodPlus" ; 



    cmd.AddValue("int_bottleneck_datarate","BottleNeck Datarate" , int_bottleneck_datarate) ;
    cmd.AddValue("packet_loss","Packet_loss_rate" , packet_loss) ;
    cmd.AddValue("algo2","The second algorithm to follow",algo2) ; 






    cmd.Parse(argc,argv) ;
    
   // std::cout<<"Algorithm 2 is : "<<algo2<<" -->Running simulation with : DataRate"<<int_bottleneck_datarate << "Mbps -- Packet loss rate : "<<packet_loss<<endl ; 


    string  bottleneck_datarate= to_string(int_bottleneck_datarate)+"Mbps" ; 
    //............................................
    Config::SetDefault("ns3::TcpSocket::SegmentSize", UintegerValue(packet_size));
    //...........................................





    Time::SetResolution(Time::NS) ;
// ------------------------------------


// ------------------------------------- NODE CREATION 

// the nodes creation will be handled by the dumbell since it's constructor does the job 
// creates both the routers and the leaf nodes . 
// will be done after we set up the corresponding helpers 


//***************************************** HELPER CREATION


    //setting up the bottleneck properties 
    PointToPointHelper XY_p2pHelper;
    XY_p2pHelper.SetDeviceAttribute ("DataRate",StringValue (bottleneck_datarate));
    XY_p2pHelper.SetChannelAttribute ("Delay",StringValue (bottleneck_delay));

    PointToPointHelper X_leaf_Y_leaf;
    X_leaf_Y_leaf.SetDeviceAttribute ("DataRate",StringValue (data_rate));
    X_leaf_Y_leaf.SetChannelAttribute ("Delay",StringValue (normal_delay));

    //uint32_t nLeftLeaf,  PointToPointHelper leftHelper,uint32_t nRightLeaf,PointToPointHelper rightHelper, PointToPointHelper bottleneckHelper
    PointToPointDumbbellHelper dumbbell(left_right_nodes , X_leaf_Y_leaf , left_right_nodes , X_leaf_Y_leaf , XY_p2pHelper) ; 


    //To set the router buffer capacity to the bandwidth delay product
    // to get the buffer employ drop-tail discarding



    double bandwidth_delay_product=(1.0*int_bottleneck_delay*int_bottleneck_datarate)/packet_size; 
    X_leaf_Y_leaf.SetQueue("ns3::DropTailQueue", "MaxSize",StringValue (std::to_string (bandwidth_delay_product) + "p"));

    Ptr<RateErrorModel> em = CreateObject<RateErrorModel>();
    em->SetAttribute("ErrorRate", DoubleValue(packet_loss));
    // m_routerDevices is the netdeviceContainer . 
    dumbbell.m_routerDevices.Get(1)->SetAttribute("ReceiveErrorModel", PointerValue(em));
    dumbbell.m_routerDevices.Get(1)->SetAttribute("ReceiveErrorModel", PointerValue(em));





   

//*****************************************

//----------------------------------------- SETUP PROTOCOL 

    

    //By setting the default TCP socket type in your NS-3 simulation, you are effectively specifying which TCP variant you want to use as the default throughout your simulation. 
    Config::SetDefault("ns3::TcpL4Protocol::SocketType", StringValue (algo1));

    // getLeft    ===> returns the router 
    // getLeft(i) ===> returns the leaf nodes 
    InternetStackHelper stack1;



    //For the bottleneck devices, you can install the corresponding internet stackwith TCP NewReno.
    stack1.Install(dumbbell.GetLeft()) ; 
    stack1.Install(dumbbell.GetRight()); 



    stack1.Install(dumbbell.GetLeft(0)) ; 
    stack1.Install(dumbbell.GetRight(0));
    
    Config::SetDefault("ns3::TcpL4Protocol::SocketType", StringValue (algo2));
    stack1.Install(dumbbell.GetLeft(1)) ; 
    stack1.Install(dumbbell.GetRight(1));





//-----------------------------------------------------

//..................................................... ASSIGN ip Address



Ipv4AddressHelper left_leaf_ip ;
left_leaf_ip.SetBase("10.1.1.0","255.255.255.0") ;
Ipv4AddressHelper right_leaf_ip ;
right_leaf_ip.SetBase("10.2.1.0","255.255.255.0") ;
Ipv4AddressHelper router_ip ;
router_ip.SetBase("10.3.1.0","255.255.255.0") ;


//Ipv4AddressHelper leftIp,Ipv4AddressHelper rightIp,Ipv4AddressHelper routerIp
dumbbell.AssignIpv4Addresses(left_leaf_ip,right_leaf_ip,router_ip) ; 

//.....................................................



 //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    Ipv4GlobalRoutingHelper::PopulateRoutingTables() ;
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>



//***************************************************** SETUP FLOW MONITOR 

FlowMonitorHelper fm ; 
// attaches the flow monitor to all nodes in the simulation.
Ptr<FlowMonitor> flowmonitor = fm.InstallAll() ; 

//*****************************************************

//---------------------------------------------------- SETUP APPLICATION 

// same format as sixth.cc 



uint16_t sinkPort=9;
PacketSinkHelper packetSinkHelper("ns3::TcpSocketFactory",InetSocketAddress(Ipv4Address::GetAny(), sinkPort));
ApplicationContainer sinkApps = packetSinkHelper.Install({dumbbell.GetRight(0),dumbbell.GetRight(1)});

sinkApps.Start(Seconds(0.0));
sinkApps.Stop(Seconds(total_time));


for(int i=0 ; i<n_flow ; i++)
{
 
    Address sinkAddress(InetSocketAddress(dumbbell.GetRightIpv4Address(i), sinkPort));
    
   
    

    Ptr<Socket> ns3TcpSocket = Socket::CreateSocket(dumbbell.GetLeft(i), TcpSocketFactory::GetTypeId());
    if(i==0)
        ns3TcpSocket->TraceConnectWithoutContext("CongestionWindow",MakeCallback(&CwndChange)) ; 
    else if(i==1)
        ns3TcpSocket->TraceConnectWithoutContext("CongestionWindow",MakeCallback(&CwndChange2)) ; 

    Ptr<TutorialApp> app = CreateObject<TutorialApp>();
    app->Setup(ns3TcpSocket, sinkAddress, packet_size, 1000000000 , DataRate(data_rate));
    dumbbell.GetLeft(i)->AddApplication(app);
    app->SetStartTime(Seconds(1.0));
    app->SetStopTime(Seconds(total_time));
    

}



    Simulator::Stop(Seconds(total_time));
    Simulator::Run();


//-----------------------------------------------------
// printing format from wifi-hidden-terminal 

    
    int algo1_rx = 0 ; 
    int algo2_rx = 0 ; 
    int algo1_tx = 0 ; 
    int algo2_tx = 0 ; 

    int jain_c=0 ; 
    double jain_a=0;
    double jain_b=0;
    double thr = 0 ; 

    flowmonitor->CheckForLostPackets();
    Ptr<Ipv4FlowClassifier> classifier = DynamicCast<Ipv4FlowClassifier>(fm.GetClassifier());
    FlowMonitor::FlowStatsContainer stats = flowmonitor->GetFlowStats();
    for (std::map<FlowId, FlowMonitor::FlowStats>::const_iterator i = stats.begin(); i != stats.end();++i)
    {
        
           /* Ipv4FlowClassifier::FiveTuple t = classifier->FindFlow(i->first);
            
            std::cout << "Flow " << i->first << " (" << t.sourceAddress << " -> "<< t.destinationAddress << ")\n";
            std::cout << "  Tx Packets: " << i->second.txPackets << "\n";
            std::cout << "  Tx Bytes:   " << i->second.txBytes << "\n";
            std::cout << "  TxOffered:  " << i->second.txBytes * 8.0 / 9.0 / 1000 / 1000<< " Mbps\n";
            std::cout << "  Rx Packets: " << i->second.rxPackets << "\n";
            std::cout << "  Rx Bytes:   " << i->second.rxBytes << "\n";
            std::cout << "  Throughput: " << i->second.rxBytes * 8.0 / 9.0 / 1000 / 1000<< " Mbps\n";
            */
            if(i->first%2==1) 
            {
                algo1_rx+=i->second.rxBytes * 8; 
                algo1_tx+=i->second.txBytes * 8; 
            }
            else 
            {
                algo2_rx+=i->second.rxBytes * 8 ; 
                algo2_tx+=i->second.txBytes * 8 ; 
            }
            thr=i->second.rxBytes * 8.0 / (total_time+5.0)*1000*1000 ;
            jain_a+=thr ; 
            jain_b+=thr*thr;
            jain_c++ ;
        
    }
    double j_index = pow(jain_a,2)/(jain_c*jain_b) ; 
    double average_throughput_1 = (1.0*algo1_rx)/(total_time*1000*1000) ; 
    double average_throughput_2 = (1.0*algo2_rx)/(total_time*1000*1000)  ;

    double xx = log10(packet_loss) ; 
    cout<< int_bottleneck_datarate <<" "<< xx <<" "<<average_throughput_1<<" "<<average_throughput_2<<" "<<j_index<<endl  ; 
    

  


    Simulator::Destroy();





} 
