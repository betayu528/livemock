package mock.web;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import mock.model.BaseClient;
import mock.model.ClientModel;
import mock.model.MockErrcode;
import mock.model.MockLiveClient;
import mock.model.ResponseModel;
import mock.service.MockClientManager;
import mock.service.MockClientOperateService;
import mock.service.MockerUdpSocketService;
import mock.service.TrackerMessageHandleService;


@RestController
@RequestMapping("/mock/tracker")
public class TrackerTestApiController {
    private static final Logger logger = LoggerFactory.getLogger(TrackerTestApiController.class);

    @Autowired 
    MockClientOperateService clientService;

    @Autowired
    MockerUdpSocketService mockerUdpClientService;

    @Autowired
    TrackerMessageHandleService messageHandleService;

    @GetMapping("/userclient/{clientId}")
    public ResponseModel getUserClient(@PathVariable Long clientId) {
        ResponseModel response = new ResponseModel();
        //BaseClient client = mockClientManagerService.getClient(clientId);
        ClientModel model = new ClientModel();
        response.setData(model);
        response.setErrcode(MockErrcode.RSP_OK);
        response.setErrMsg("ok");
        return response;
    }

    @RequestMapping(value = "/trackerLivePackInfo", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseModel trackerMessageInfo() {
        //往发送队列里投递一个包 
        //mockClientManagerService.
        ByteBuffer message =  ByteBuffer.wrap(messageHandleService.buildLoginTrackerPacket(123456789L, 0, 0));
        ResponseModel model = new ResponseModel();
        model.setData(message.toString());
        Map<String, Object> data = new HashMap<>();
        data.put("message", message.toString());
        data.put("bytes", message.array());
        data.put("length", message.capacity());
        model.setData(data);
        return model;
    }

    @RequestMapping(value = "/loginUser", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseModel loginUser(HttpServletRequest request, HttpServletResponse response) {
        ResponseModel responseModel = new ResponseModel();
        return responseModel;
    }
    @RequestMapping(value = "/testLoginUser", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseModel loginUser() {
        ResponseModel responseModel = new ResponseModel();
        return responseModel;
    }

    @RequestMapping("/makeTrackerEmptyMsg")
    public ResponseModel makeEmptyMessage() {
        ByteBuffer byteBuffer = messageHandleService.makeEmptyPacket();
        ResponseModel model = new ResponseModel();
        model.setData(byteBuffer.array());
        model.setErrMsg("ok");
        model.setErrcode(0);
        return model;
    }


    @RequestMapping("/newclient")
    public ResponseModel newClient(HttpServletRequest request) {
        String streamId = request.getParameter("streamId");
        String channelId = request.getParameter("channelId");
        Boolean needLogin = Boolean.valueOf(request.getParameter("login")); 
        
        ResponseModel responseModel = new ResponseModel();
        if (StringUtils.isEmpty(streamId) || StringUtils.isEmpty(channelId)) {
            responseModel.setErrMsg("streamId or channelId is empty!");
            responseModel.setErrcode(10001);
            return responseModel;
        }
        String type = request.getParameter("clientType");
        if (StringUtils.isEmpty(type)) {
            type = "live";
        }
        BaseClient client = clientService.createClient(type, channelId, streamId);
        if (client == null) {
            responseModel.setErrMsg("create fail");
            responseModel.setErrcode(10002);
            return responseModel;
        }

        if (needLogin) {
            Boolean b = clientService.loginUser(client.getClientId());
            logger.info("client {} try to login traker", client.getClientId());
            if (!b) {
                logger.error("client {} login failed", client.getClientId());
            }
        }
        
        Map<String, Object> clientInfo = new HashMap<>();
        clientInfo.put("clientId", client.getClientId());
        clientInfo.put("mac", client.getDid());
        clientInfo.put("listenAddr", client.getListenAddress());
        clientInfo.put("releaseId", client.getReleaseId());
        clientInfo.put("relayServerList", client.getRelayServerList());
        clientInfo.put("status", client.getStatus());
        clientInfo.put("trackerServer", client.getTrackerServer());
        clientInfo.put("clientIp",client.getClientIp());
        if (type == "live") {
            MockLiveClient liveClient = (MockLiveClient)client;
            clientInfo.put("liveAddresss", liveClient.getLiveServerAddressList());
        }
        
        responseModel.setData(clientInfo);
        return responseModel;
    }

    @RequestMapping("/getallclients")
    public ResponseModel getAllClients(HttpServletRequest request) {
        ResponseModel responseModel = new ResponseModel();
        List<BaseClient> clients = MockClientManager.getInstance().getAllClients();
        List<ClientModel> clientInfos = new ArrayList<>();
        for (BaseClient client: clients) {
            ClientModel model = new ClientModel();
            model.setClientId(client.getClientId());
            //model.setClientIp(client.getClientIp());
            model.setDid(client.getDid());
            model.setListenAddress(client.getListenAddress());
            model.setName("test_" + client.getClientId());
            model.setRelayServerList(client.getRelayServerList());
            
            model.setReleaseId(client.getReleaseId());
            model.setStatus(client.getStatus());
            model.setLiveServerList(client.getLiveServerAddressList());
            
            clientInfos.add(model);
        }
        
        responseModel.setData(clientInfos);
        return responseModel;
    }
}
