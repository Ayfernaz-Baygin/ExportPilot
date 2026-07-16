package com.exportpilot.analysisresult.service;

import com.exportpilot.analysis.repository.AnalysisRepository;
import com.exportpilot.analysisresult.dto.AnalysisCountryResultResponse;
import com.exportpilot.analysisresult.entity.AnalysisCountryResult;
import com.exportpilot.analysisresult.mapper.AnalysisCountryResultMapper;
import com.exportpilot.analysisresult.repository.AnalysisCountryResultRepository;
import com.exportpilot.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnalysisCountryResultService {

    private final AnalysisCountryResultRepository resultRepository;
    private final AnalysisCountryResultMapper resultMapper;
    private final AnalysisRepository analysisRepository;

    public AnalysisCountryResultService(
            AnalysisCountryResultRepository resultRepository,
            AnalysisCountryResultMapper resultMapper,
            AnalysisRepository analysisRepository
    ) {
        this.resultRepository = resultRepository;
        this.resultMapper = resultMapper;
        this.analysisRepository = analysisRepository;
    }

    @Transactional(readOnly = true)
    public List<AnalysisCountryResultResponse> getResultsByAnalysisId(
            Long analysisId
    ) {
        ensureAnalysisExists(analysisId);

        return resultMapper.toResponseList(
                resultRepository
                        .findAllByAnalysisIdOrderByRankPositionAsc(
                                analysisId
                        )
        );
    }

    @Transactional(readOnly = true)
    public AnalysisCountryResultResponse getResultById(Long id) {
        AnalysisCountryResult result = resultRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Analysis country result not found with id: "
                                        + id
                        )
                );

        return resultMapper.toResponse(result);
    }

    @Transactional(readOnly = true)
    public AnalysisCountryResultResponse getResultByAnalysisAndCountry(
            Long analysisId,
            Long countryId
    ) {
        ensureAnalysisExists(analysisId);

        AnalysisCountryResult result = resultRepository
                .findByAnalysisIdAndCountryId(
                        analysisId,
                        countryId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Analysis result not found for analysis id: "
                                        + analysisId
                                        + " and country id: "
                                        + countryId
                        )
                );

        return resultMapper.toResponse(result);
    }

    private void ensureAnalysisExists(Long analysisId) {
        if (!analysisRepository.existsById(analysisId)) {
            throw new ResourceNotFoundException(
                    "Analysis not found with id: " + analysisId
            );
        }
    }
}