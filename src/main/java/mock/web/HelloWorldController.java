package mock.web;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mock.model.HelloModel;
import rapid.cloud.cdn.protocol.utils.P2PUtils;

/*调试用途*/
@RestController("/")
public class HelloWorldController {
    
	@RequestMapping("/")
	String home() {
		return "Hello World!";
	}

	@GetMapping("/home")
	public String homeIndex() {
		return "myhome";
	}

	@RequestMapping("/test") 
	public String test (){
		return "{}";
	}

    @GetMapping("/gethello")
    public HelloModel getHello() {
        return new HelloModel();
    }

    @GetMapping("/getMultiHello") 
    public List<HelloModel> getMultiHello() {
        List<HelloModel> hellos = new ArrayList<>();
        hellos.add(new HelloModel(100, 1000, 2000, 5000, 10, 55));
        hellos.add(new HelloModel());
        return hellos;
    }

    @GetMapping("/getMultiHelloV2") 
    public Map<String, Object> getMultiHelloV2() {
        Map<String, Object> res = new HashMap<>();
        List<HelloModel> hellos = new ArrayList<>();
        hellos.add(new HelloModel(100, 1000, 2000, 5000, 10, 55));
        hellos.add(new HelloModel());
        res.put("res", new Integer(0));
        res.put("error_msg", new String("ok"));
        res.put("data", hellos);
        return res;
    }

    public static void main(String[] args) {
        InetSocketAddress addr = P2PUtils.hostToSocketAddr("10.10.4.89:9000");
        System.out.println(addr.getHostName());
        System.out.println(addr.getPort());
        System.out.println(addr.getAddress().toString());
    }
}
