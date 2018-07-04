package com.spothero.api.controller;

import com.spothero.api.ParkingRateCalculator;
import com.spothero.api.model.ParkingRate;
import com.spothero.api.model.ParkingRateListWrapper;
import com.spothero.api.repository.ParkingRateRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ParkingRateCalculator.class)
@WebAppConfiguration
public class ParkingRateControllerTest {

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    private HttpMessageConverter mappingJackson2HttpMessageConverter;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ParkingRateRepository parkingRateRepository;

    @Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
                .filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .orElse(null);

        assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        parkingRateRepository.deleteAllInBatch();

    }

    @Test
    public void testCalculateRateByHour() throws Exception {

        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0600-1400", 1500));

        String startTime = "2015-07-01T07:00:00";
        String endTime = "2015-07-01T13:00:00";

        this.mockMvc.perform(get("/rates?startTime=" + startTime + "&endTime=" + endTime)
                .contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(content().json("1500"));
    }

    @Test
    public void testCalculateRateByMinute() throws Exception {

        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0700-1300", 1500));


        String startTime = "2015-07-01T07:01:00";
        String endTime = "2015-07-01T12:59:00";

        this.mockMvc.perform(get("/rates?startTime=" + startTime + "&endTime=" + endTime)
                .contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(content().json("1500"));
    }

    @Test
    public void testCalculateRateBySecond() throws Exception {

        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0700-1300", 1500));


        String startTime = "2015-07-01T07:00:01";
        String endTime = "2015-07-01T12:59:59";

        this.mockMvc.perform(get("/rates?startTime=" + startTime + "&endTime=" + endTime)
                .contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(content().json("1500"));
    }

    @Test
    public void testCalculateRateMinutes() throws Exception {

        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0723-1345", 1500));


        String startTime = "2015-07-01T07:24:00";
        String endTime = "2015-07-01T13:44:00";

        this.mockMvc.perform(get("/rates?startTime=" + startTime + "&endTime=" + endTime)
                .contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(content().json("1500"));
    }

    @Test
    public void testCalculateRateOnSameDay() throws Exception {

        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0700-1300", 1500));
        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0400-0600", 1000));
        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "1400-2000", 2000));


        String startTime = "2015-07-01T08:00:00";
        String endTime = "2015-07-01T12:00:00";

        this.mockMvc.perform(get("/rates?startTime=" + startTime + "&endTime=" + endTime)
                .contentType(contentType))
                .andExpect(status().isOk())
                .andExpect(content().contentType(contentType))
                .andExpect(content().json("1500"));
    }

    @Test
    public void testCalculateRateUnavailable() throws Exception {

        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0700-1300", 1500));

        String startTime = "2015-07-04T06:00:00";
        String endTime = "2015-07-04T20:00:00";

        this.mockMvc.perform(get("/rates?startTime=" + startTime + "&endTime=" + endTime)
                .contentType(contentType))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void testCalculateRateUnavailableLowerBounds() throws Exception {

        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0700-1300", 1500));

        String startTime = "2015-07-04T07:00:00";
        String endTime = "2015-07-04T12:00:00";

        this.mockMvc.perform(get("/rates?startTime=" + startTime + "&endTime=" + endTime)
                .contentType(contentType))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void testCalculateRateUnavailableUpperBounds() throws Exception {

        parkingRateRepository.save(new ParkingRate("mon,tues,wed,thurs,fri,sat,sun", "0700-1300", 1500));

        String startTime = "2015-07-04T09:00:00";
        String endTime = "2015-07-04T13:00:00";

        this.mockMvc.perform(get("/rates?startTime=" + startTime + "&endTime=" + endTime)
                .contentType(contentType))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void testPutRates() throws Exception{
        List<ParkingRate> rates = new ArrayList<>();
        rates.add(new ParkingRate("mon,tues", "0800-1400", 1200));
        rates.add(new ParkingRate("thur,fri", "0800-1400", 1400));
        rates.add(new ParkingRate("sat,sun", "0800-1400", 1600));

        ParkingRateListWrapper pr = new ParkingRateListWrapper(rates);

        this.mockMvc.perform(put("/rates")
                .contentType(contentType)
                .content(json(pr)))
                .andExpect(status().isCreated());

        assertEquals(3, parkingRateRepository.findAll().size());

    }

    @Test
    public void testPutRatesOverwrite() throws Exception{
        parkingRateRepository.save(new ParkingRate("mon", "0800-0900", 1000));

        List<ParkingRate> rates = new ArrayList<>();
        rates.add(new ParkingRate("mon,tues", "0800-1400", 1200));
        rates.add(new ParkingRate("thur,fri", "0800-1400", 1400));
        rates.add(new ParkingRate("sat,sun", "0800-1400", 1600));

        ParkingRateListWrapper pr = new ParkingRateListWrapper(rates);

        this.mockMvc.perform(put("/rates")
                .contentType(contentType)
                .content(json(pr)))
                .andExpect(status().isCreated());

        assertEquals(3, parkingRateRepository.findAll().size());

    }

    private String json(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }
}
