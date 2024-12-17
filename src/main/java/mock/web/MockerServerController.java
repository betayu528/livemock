package mock.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mock.model.ResponseModel;
import mock.service.MockerUdpSocketService;

@RestController
@RequestMapping("/mock")
public class MockerServerController {
    @Autowired
    private MockerUdpSocketService mockerUdpSocketService;

    @GetMapping("/queueNum")
    public ResponseModel getQueueNumber() {
        Integer number = mockerUdpSocketService.getSendQueueNumber();
        ResponseModel model = new ResponseModel();
        model.setErrcode(0);
        model.setErrMsg("ok");
        Map<String, Object> data = new HashMap<>();
        data.put("number", number);
        model.setData(data);
        return model;
    }
}
