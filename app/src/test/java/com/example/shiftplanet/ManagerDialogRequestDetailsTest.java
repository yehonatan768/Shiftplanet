package com.example.shiftplanet;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Intent;
import android.widget.Toast;

import androidx.test.core.app.ApplicationProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManagerDialogRequestDetailsTest {

    @Mock
    private FirebaseFirestore mockDb;

    @Mock
    private DocumentSnapshot mockRequestDocument;

    @Mock
    private DocumentReference mockDocumentReference;

    private ManagerDialogRequestDetails activity;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        activity = new ManagerDialogRequestDetails();
        activity.db = mockDb;
        activity.requestDocument = mockRequestDocument;
        activity.managerEmail = "yehonatan768@gmail.com";

        // Mock request data
        when(mockRequestDocument.getId()).thenReturn("testRequestId");
        when(mockRequestDocument.getString("details")).thenReturn("This is a sample request for leave approval.");
        when(mockRequestDocument.getString("employeeEmail")).thenReturn("mekayten100@gmail.com");
        when(mockRequestDocument.getString("endDate")).thenReturn("30/1/2025");
        when(mockRequestDocument.getString("managerEmail")).thenReturn("yehonatan768@gmail.com");
        when(mockRequestDocument.getString("reason")).thenReturn("Vacation");
        when(mockRequestDocument.getLong("requestNumber")).thenReturn(111L);
        when(mockRequestDocument.getString("startDate")).thenReturn("23/1/2025");
        when(mockRequestDocument.getString("status")).thenReturn("pending");
    }

    @Test
    public void testHandleApproveAction() {
        when(mockDb.collection("Requests").document("testRequestId")).thenReturn(mockDocumentReference);
        Task<Void> mockTask = mock(Task.class);
        when(mockDocumentReference.update("status", "approved")).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockTask;
        });

        activity.handleApproveAction();

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockDocumentReference).update(eq("status"), statusCaptor.capture());
        assertEquals("approved", statusCaptor.getValue());
    }

    @Test
    public void testHandleDenyAction() {
        when(mockDb.collection("Requests").document("testRequestId")).thenReturn(mockDocumentReference);
        Task<Void> mockTask = mock(Task.class);
        when(mockDocumentReference.update("status", "denied")).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            OnSuccessListener<Void> listener = invocation.getArgument(0);
            listener.onSuccess(null);
            return mockTask;
        });

        activity.handleDenyAction();

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockDocumentReference).update(eq("status"), statusCaptor.capture());
        assertEquals("denied", statusCaptor.getValue());
    }
}
