package com.megogo.tests.api;

import com.megogo.dto.ChannelDto;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static java.time.ZonedDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.hamcrest.Matchers.equalTo;

public class MegogoTests {

    @BeforeSuite
    public void setupRestAssured() {
        RestAssured.baseURI = "https://epg.megogo.net";
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(), new AllureRestAssured());
    }

    @Test
    public void testExpectCorrectServerTime() {
        Response response = given()
                .when()
                .get("/time")
                .then().statusCode(200)
                .body("result", equalTo("ok"))
                .extract().response();

        long serverTime = response
                .then()
                .extract()
                .body().jsonPath().getLong("data.timestamp");

        long currentTime = System.currentTimeMillis();
        long latency = response.getTime();
        long expectedServerTime = (currentTime - latency) / 1000;
        assertThat(serverTime).isCloseTo(expectedServerTime, within(1L));
    }

    @DataProvider(parallel = true)
    public Object[][] videoIds() {
        return new Object[][]{
                {1639111},
                {1585681},
                {1639231}
        };
    }

    @Test(dataProvider = "videoIds")
    public void testExpectedProgramsSortedByStartTime(long videoId) {
        ChannelDto channel = getChannelInfo(videoId);

        List<Long> listOfStartTime = channel.getData().get(0)
                .getPrograms().stream()
                .map(ChannelDto.DataDto.Program::getStartTimestamp)
                .collect(Collectors.toList());

        assertThat(listOfStartTime)
                .as("Is sorted")
                .isSorted();
    }

    @Test(dataProvider = "videoIds")
    public void testExpectCurrentProgramExists(long videoId) {
        ChannelDto channels = getChannelInfo(videoId);

        ChannelDto.DataDto.Program currentProgram = channels.getData().get(0).getPrograms().get(0);
        long currentTime = System.currentTimeMillis() / 1000;
        assertThat(currentTime)
                .as("Current time is between start and end time of the current program")
                .isBetween(currentProgram.getStartTimestamp(), currentProgram.getEndTimestamp());

    }

    @Test(dataProvider = "videoIds")
    public void testExpectChannelHasNoPastProgram(long videoId) {
        ChannelDto channels = getChannelInfo(videoId);

        ChannelDto.DataDto.Program currentProgram = channels.getData().get(0).getPrograms().get(0);
        long currentTime = System.currentTimeMillis() / 1000;
        assertThat(currentProgram.getEndTimestamp())
                .as("Current time is less than end time of the current program")
                .isGreaterThan(currentTime);
    }

    @Test(dataProvider = "videoIds")
    public void testExpectChannelHasNoFutureProgram(long videoId) {
        ChannelDto channels = getChannelInfo(videoId);

        //reversing the list to get the last program
        Collections.reverse(channels.getData().get(0).getPrograms());
        ChannelDto.DataDto.Program latestProgram = channels.getData().get(0).getPrograms().get(0);

        long timeIn24Hours = now().plusDays(1).toEpochSecond();
        assertThat(latestProgram.getStartTimestamp())
                .as("Latest program start time is less than 24 hours")
                .isLessThan(timeIn24Hours);
    }

    private ChannelDto getChannelInfo(long videoId) {
        return given()
                .when()
                .param("video_ids", videoId)
                .get("/channel")
                .then()
                .statusCode(200)
                .body("result", equalTo("ok"))
                .extract().as(ChannelDto.class);
    }
}
