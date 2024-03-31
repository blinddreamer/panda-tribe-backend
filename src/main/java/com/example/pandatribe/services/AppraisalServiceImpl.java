package com.example.pandatribe.services;

import com.example.pandatribe.feign.contracts.EveInteractor;
import com.example.pandatribe.models.requests.AppraisalRequest;
import com.example.pandatribe.models.results.AppraisalResult;
import com.example.pandatribe.services.contracts.AppraisalService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AppraisalServiceImpl implements AppraisalService {
    private final EveInteractor eveInteractor;

    @Override
    public AppraisalResult generateAppraisalResult(AppraisalRequest appraisalRequest) {
        return null;
    }
}
