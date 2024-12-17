package mock.web;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import mock.model.ResponseModel;
import mock.service.MockerUdpSocketService;
import mock.service.TrackerMessageHandleService;
import rapid.cloud.cdn.common.mrttoken.MRTToken;
import rapid.cloud.cdn.common.mrttoken.MRTTokenTools;
import rapid.cloud.cdn.protocol.utils.P2PUtils;

/*测试工具接口 */
/*
 * /mrttokendecode mrttoken解码
 * /mrttokenencode mrttoken加密
 */
@RestController
@RequestMapping("/tool")
public class RapidToolController {
    @Autowired
    MockerUdpSocketService mockerUdpClientService;

    @Autowired
    TrackerMessageHandleService messageHandleService;
    
    @RequestMapping("/mrttokendecode")
    public ResponseModel mrtDecode(HttpServletRequest request) {
        String encryptedStr = request.getParameter("encryptedStr");
        String aesKey = request.getParameter("aesKey");
        if (aesKey == null) {
            aesKey = "gvqlgfwB6oufvfO4j=jb";
        }
        if (StringUtils.isEmpty(encryptedStr) || StringUtils.isEmpty(aesKey)) {
            return new ResponseModel("param encryptedStr and aesKey are needed", 10003);
        }
        MRTToken token = MRTTokenTools.decode(encryptedStr, aesKey);
        ResponseModel response = new ResponseModel();
        response.setData(token);
        response.setErrMsg("ok");
        response.setErrcode(0);
        return response;
    }

    @RequestMapping("/mrttokenencode")
    public ResponseModel mrtEncode(@RequestBody MRTToken token,  HttpServletRequest request) {
        String aesKey = request.getParameter("aesKey");
        if (token == null || StringUtils.isEmpty(aesKey)) {
            return new ResponseModel("MRTToken object and aesKey are needed", 10003);
        }

        String secruitStr = MRTTokenTools.encode(token, aesKey);
        return new ResponseModel("ok", 0, secruitStr);
    }

    
    @RequestMapping("/makeLoginPacket")
    public ResponseModel makeLoginPacket() {
        Long connetId = new Long("123456567577");
        byte [] buff =  messageHandleService.buildLoginTrackerPacket(connetId, 0, 0);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buff);
        InetSocketAddress addr = P2PUtils.hostToSocketAddr("147.135.44.184:37000");
        InetSocketAddress [] addrs = new InetSocketAddress[1];
        addrs[0] = addr;
        mockerUdpClientService.sendPacket(byteBuffer, addrs, connetId, false);
        ResponseModel model = new ResponseModel();

        model.setErrMsg("ok");
        model.setErrcode(0);
        model.setData(buff);
        return model;
    }
    
    public static void main(String[] args) {
        MRTToken token = MRTTokenTools.decode("SPIEOskLh84qNj2Oo_7HHX5IWBS3jRg9x0Rw4PBZvD8QDZkDrHwcLc8PVD92veiEsW5aXWyYo2TXyyrUh64Fu2nXvZ2jlJg2Z4XpurwjIJI",
        "gvqlgfwB6oufvfO4j=jb");
       System.out.println(new Gson().toJson(token));
    }
}
