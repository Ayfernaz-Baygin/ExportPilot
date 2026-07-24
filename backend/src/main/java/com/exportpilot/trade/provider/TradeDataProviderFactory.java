package com.exportpilot.trade.provider;

import com.exportpilot.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class TradeDataProviderFactory {

    private final Map<
            TradeDataSourceType,
            TradeDataProvider
            > providers;

    public TradeDataProviderFactory(
            List<TradeDataProvider> providerList
    ) {
        this.providers =
                new EnumMap<>(TradeDataSourceType.class);

        for (TradeDataProvider provider : providerList) {
            TradeDataProvider existingProvider =
                    providers.put(
                            provider.getType(),
                            provider
                    );

            if (existingProvider != null) {
                throw new IllegalStateException(
                        "Multiple trade data providers registered for type: "
                                + provider.getType()
                );
            }
        }
    }

    public TradeDataProvider getProvider(
            TradeDataSourceType sourceType
    ) {
        if (sourceType == null) {
            throw new BusinessRuleException(
                    "Trade data source type is required."
            );
        }

        TradeDataProvider provider =
                providers.get(sourceType);

        if (provider == null) {
            throw new BusinessRuleException(
                    "Trade data provider is not available for source type: "
                            + sourceType
            );
        }

        return provider;
    }
}