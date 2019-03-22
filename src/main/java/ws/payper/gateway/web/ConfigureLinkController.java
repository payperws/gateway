package ws.payper.gateway.web;


import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ws.payper.gateway.PayableLink;
import ws.payper.gateway.config.PaymentOptionType;
import ws.payper.gateway.lightning.LightningConnector;
import ws.payper.gateway.model.CryptoCurrency;
import ws.payper.gateway.service.PaymentOptions;
import ws.payper.gateway.service.PaymentOptionsService;
import ws.payper.gateway.service.RouteService;
import ws.payper.gateway.util.  PaymentUriHelper;

import java.math.BigDecimal;
import java.util.Map;

@Controller
public class ConfigureLinkController {

    @Autowired
    private PaymentUriHelper uriBuilder;

    @Autowired
    private RouteService routeService;

    @Autowired
    private LightningConnector lightningConnector;

    @Autowired
    private PaymentOptionsService paymentOptionsService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showNewLinkPage() {
        return "configure-link";
    }

    @RequestMapping(value = "/ok", method = RequestMethod.GET)
    public String linkCreated(@RequestParam String url, Model model) {
        model.addAttribute("payableLink", url);
        return "link-created";
    }

    @PostMapping(value = "/link")
    @ResponseBody
    public
    Mono<PayableLink> newLink(@RequestBody LinkConfig link) {
        String payableId = RandomStringUtils.randomAlphanumeric(10);
        String payableUrl = uriBuilder.payableUri(payableId).toString();
        String payablePath = uriBuilder.payablePath(payableId);
        PayableLink payable = new PayableLink(link, payableId, payableUrl, payablePath);

        return routeService.registerAndRefresh(payable);
    }

    @PostMapping(value = "/ln-check")
    @ResponseBody
    public NodeCheckResponse checkNode(@RequestBody NodeCheckRequest request) {
        String host = request.getPubkeyHost();
        String port = request.getRpcport();
        String tlsCert = request.getTlsCert();
        String macaroon = request.getInvoiceMacaroon();
        boolean checked = lightningConnector.checkNode(host, port, tlsCert, macaroon);
        String errorMsg = checked ? null : "Could not connect to Lightning node. Check your connection details.";
        return new NodeCheckResponse(checked, errorMsg);
    }


    @RequestMapping(value = "/pay-options", method = RequestMethod.GET)
    @ResponseBody
    public PaymentOptions availablePaymentOptions() {
        return paymentOptionsService.availablePaymentOptions();
    }

    public static class LinkConfig {

        private String url;

        private PaymentOptionType paymentOptionType;

        private Map<String, String> paymentOptionArgs;

        private BigDecimal price;

        private CryptoCurrency currency;

        private String title;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public PaymentOptionType getPaymentOptionType() {
            return paymentOptionType;
        }

        public void setPaymentOptionType(PaymentOptionType paymentOptionType) {
            this.paymentOptionType = paymentOptionType;
        }

        public Map<String, String> getPaymentOptionArgs() {
            return paymentOptionArgs;
        }

        public void setPaymentOptionArgs(Map<String, String> paymentOptionArgs) {
            this.paymentOptionArgs = paymentOptionArgs;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public CryptoCurrency getCurrency() {
            return currency;
        }

        public void setCurrency(CryptoCurrency currency) {
            this.currency = currency;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

}
