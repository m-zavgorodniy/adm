package servlets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;

class GroupServletTest {

    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @Mock
    ServletConfig sconfig;

    @Mock
    ServletContext scontext;

    // @Before
    void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGroupNotFound() throws Exception {

        setup();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(response.getWriter()).thenReturn(pw);

        when(request.getRequestURI()).thenReturn("/api/groups/5d8a97007c971261c4aecb78");

        ArgumentCaptor<Integer> error_code = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> stringArg = ArgumentCaptor.forClass(String.class);
        doNothing().when(response).sendError(error_code.capture(), stringArg.capture());

        when(sconfig.getServletContext()).thenReturn(scontext);
        when(scontext.getInitParameter("mongodbConnectString")).thenReturn("mongodb://max:2UbwJ2koHiGNMvCk@cluster0-shard-00-00-mqypc.mongodb.net:27017,cluster0-shard-00-01-mqypc.mongodb.net:27017,cluster0-shard-00-02-mqypc.mongodb.net:27017/adm?ssl=true&;replicaSet=Cluster0-shard-0&;authSource=admin&;retryWrites=true");

        GroupServlet servlet = new GroupServlet();
        servlet.init(sconfig);
        servlet.doGet(request, response);
        assertEquals(error_code.getValue(), HttpServletResponse.SC_NOT_FOUND);
    }
}