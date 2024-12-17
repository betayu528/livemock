package mock.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import mock.model.BaseClient;
import mock.model.MockLiveClient;
import mock.service.MockClientManager;
import mock.service.MockClientOperateService;


@RestController
@RequestMapping("/mock/api")
public class MockServerApiController {
    private final Logger logger = LoggerFactory.getLogger(MockServerApiController.class);
    
    @Autowired
    private  MockClientOperateService clientSerive;

    @GetMapping("/hello")
    public String testHello() {
        String info = "now";
        logger.info("%s", info);
        return "hello";
    }

    @GetMapping(value = "/login")
    public String login() {
        System.out.println("debug message");
        return "success";
    }

    @RequestMapping(value = "/getclient/{clientId}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public BaseClient getClient(@PathVariable Long clientId) {
        Gson result = new Gson();
        BaseClient client = MockClientManager.getInstance().getClient(clientId);
        if (client == null) {
            return null;
        }
        return client;
    }

    @RequestMapping(value = "/sendTcpPacket", method = {RequestMethod.GET,  RequestMethod.POST})
    public String testSendATcpPacket(HttpServletRequest request, HttpServletResponse response, @RequestBody String playLoginJson) {
        return "succ";
    }

    @RequestMapping(value = "/sendUdpPacket", method = {RequestMethod.GET, RequestMethod.POST})
    public String testSendUdpPacket(HttpServletRequest request, HttpServletResponse response, @RequestBody String sendUdpPacketRequest) {
        
        BaseClient client = clientSerive.createClient("live",
         "10043023", "368787171110289408"); // test 实际代码从资源池中获取 或者指定);
        client.login();
        MockLiveClient mclient = (MockLiveClient)client;
        //mclient.sendUdpPacketRequest();
        return "succ";
    }

    
    @GetMapping(value = "/getUdpSockectAddress")
    public Gson getUdpServerListAddress() {
        Gson resbody = new Gson();
        return resbody;
    }
}
    

