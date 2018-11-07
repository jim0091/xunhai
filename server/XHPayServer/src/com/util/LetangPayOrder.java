package com.util;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.security.PublicKey;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.eclipse.jetty.util.ajax.JSON;

/**
 * 火盒SDK辅助类
 * @author ken
 * @date 2018年10月26日
 */
public class LetangPayOrder {

    private static Logger logger = Logger.getLogger(LetangPayOrder.class);

    private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5SCpdyndz+nd9NUBSTKA"
            + "Klo9AdFxH0FaaoOHE+fI7yLPktyeIfNJr1fkbolyO11WHxnyK0dZbUupq3skjDJV"
            + "sr89QXMWm+cj54RcBOObfUU9zoZ/9lDGyoCaKPesADuWCf5zz89Km9hByV6ZuFcN"
            + "BlyRBmQDqqASfVgltMpXyDgt9ErPDdn+I+6y6OOO1N0tLT3U/skOh6zoU/zfyVtV"
            + "hSNm1+2Vv3IWqQb7YMwB1AUPke8VFejTXxE/pq3rKkQ0IO2qNOYmJTOs+V3r1/OO"
            + "Llxq4Bd82sFCZKf9VlZweOfisj4sBKJ+of6meV2LOm5zN/Xc1281wE3eln1aeWCZJQIDAQAB";

    //乐糖订单id
    private String orderId;

    //商品id
    private String productId;

    //商品金额
    private String productAtm;

    //支付类型(12[支付宝]|1301[微信]|14[苹果支付]|1401[苹果沙盒支付])
    private String orderType;

    //透传参数
    private String extra;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductAtm() {
        return productAtm;
    }

    public void setProductAtm(String productAtm) {
        this.productAtm = productAtm;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public String toString() {
        return "orderId#" + orderId + ", productId#" + productId + ", productAtm#" + productAtm
                + ", orderType#" + orderType + ", extra#" + extra;
    }

    public static LetangPayOrder createPayOrder(HttpServletRequest request) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
            String str;
            StringBuffer stringBuffer = new StringBuffer();
            while ((str = in.readLine()) != null) {
                stringBuffer.append(str);
            }
            String requestStr = stringBuffer.toString();
            if (requestStr == null) {
                logger.info("letang requestStr is null#" + requestStr);
                return null;
            }
            logger.info("letang requestStr length #" + requestStr.length());

            sun.misc.BASE64Decoder decoder = new sun.misc.BASE64Decoder();
            PublicKey publicKey =  RSA2Utils.getPublicKey("src/com/util/rsa_public_key.pem");

            int count = requestStr.length() / 344;
            if (count <= 0) {
                logger.info("letang count is zero#" + requestStr.length());
                return null;
            }

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < count; i++) {
                String section = requestStr.substring(344 * i, 344 * (i + 1));
                byte[] data = decoder.decodeBuffer(section);
                String result = RSA2Utils.RSADecode(publicKey, data);
                sb.append(result);
            }
            String lastResult = URLDecoder.decode(sb.toString(), "utf-8");
            logger.info("letang lastResult #" + lastResult);
            Map maps = (Map) JSON.parse(lastResult);
            LetangPayOrder payOrder = new LetangPayOrder();
            payOrder.setOrderId(String.valueOf(maps.get("orderId")));
            payOrder.setProductId(String.valueOf(maps.get("productId")));
            payOrder.setProductAtm(String.valueOf(maps.get("productAtm")));
            payOrder.setOrderType(String.valueOf(maps.get("orderType")));
            payOrder.setExtra(String.valueOf(maps.get("CPsendInfo")));
            logger.info("letang payOrder message #" + payOrder.toString());
            return payOrder;
        } catch (Exception e) {
            logger.error("create letang pay order error1", e);
        } finally {
            try {
                if (null != in) in.close();
                in = null;
            } catch (Exception e1) {
                logger.error("create letang pay order error2", e1);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String test = "{\"orderId\":\"17f80fc716a35e8badb8bd4245pid148\",\"productId\":\"test\",\"productAtm\":\"10\",\"orderType\":\"1301\",\"CPsendInfo\":\"51920_235373_5192000007413\"}";
        Map maps = (Map) JSON.parse(test);
        LetangPayOrder payOrder = new LetangPayOrder();
        payOrder.setOrderId(String.valueOf(maps.get("orderId")));
        payOrder.setProductId(String.valueOf(maps.get("productId")));
        payOrder.setProductAtm(String.valueOf(maps.get("productAtm")));
        payOrder.setOrderType(String.valueOf(maps.get("orderType")));
        payOrder.setExtra(String.valueOf(maps.get("CPsendInfo")));
        System.out.println(payOrder.toString());
    }

}
