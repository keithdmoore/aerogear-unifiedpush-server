/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;


import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;

import java.util.List;

public class JPAPushMessageInformationDao extends JPABaseDao implements PushMessageInformationDao {

    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    @Override
    public List<PushMessageInformation> findAllForPushApplication(String pushApplicationId, boolean ascending) {

        List<PushMessageInformation> messageInformations = createQuery("select pmi from PushMessageInformation pmi where pmi.pushApplicationId = :pushApplicationId ORDER BY pmi.submitDate " + ascendingOrDescending(ascending))
                .setParameter("pushApplicationId", pushApplicationId).getResultList();

        return messageInformations;
    }

    @Override
    public List<PushMessageInformation> findAllForVariant(String variantID, boolean ascending) {
        List<PushMessageInformation> messageInformations = createQuery("select pmi from PushMessageInformation pmi JOIN pmi.variantInformations vi where vi.variantID = :variantID ORDER BY pmi.submitDate " + ascendingOrDescending(ascending))
                .setParameter("variantID", variantID).getResultList();

        return messageInformations;
    }

    @Override
    public long getNumberOfPushMessagesForApplications(List<String> pushApplicationIds) {
        return (Long) createQuery("select count(pmi) from PushMessageInformation pmi where pmi.pushApplicationId IN :pushApplicationIds")
                .setParameter("pushApplicationIds", pushApplicationIds).getSingleResult();
    }

    @Override
    public PushMessageInformation find(String id) {
        return entityManager.find(PushMessageInformation.class, id);
    }

    @Override
    public void create(PushMessageInformation pushMessageInformation) {
        persist(pushMessageInformation);
    }

    @Override
    public void update(PushMessageInformation pushMessageInformation) {
        merge(pushMessageInformation);
    }

    @Override
    public void delete(PushMessageInformation pushMessageInformation) {
        PushMessageInformation entity = find(pushMessageInformation.getId())  ;
        remove(entity);
    }

    @Override
    public List<String> findVariantIDsWithWarnings(List<String> allVariantIDs) {
        List<String> variantIDsWithWarnings = createQuery("select vmi.variantID from VariantMetricInformation vmi" +
                " where vmi.variantID IN :variantIDs" +
                " and vmi.deliveryStatus = false")
                .setParameter("variantIDs", allVariantIDs)
                .getResultList();

        return variantIDsWithWarnings;
    }

    @Override
    public List<String> findTopThreeBusyVariantIDs(List<String> allVariantIDs) {
        List<String> variantIDsWithWarnings = createQuery("select vmi.variantID from VariantMetricInformation vmi" +
                " where vmi.variantID IN :variantIDs" +
                " ORDER BY vmi.receivers " + DESC)
                .setParameter("variantIDs", allVariantIDs)
                .setFirstResult(0)
                .setMaxResults(3)
                .getResultList();

        return variantIDsWithWarnings;
    }

    /**
     * Helper that returns 'ASC' when true and 'DESC' when false.
     */
    private String ascendingOrDescending(boolean asc) {
        if (asc) {
            return ASC;
        } else {
            return DESC;
        }
    }
}
