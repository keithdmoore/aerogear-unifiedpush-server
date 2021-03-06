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

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;

import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

public class JPAVariantDao extends JPABaseDao implements VariantDao {


    @Override
    public void create(Variant variant) {
        persist(variant);
    }

    @Override
    public void update(Variant variant) {
        merge(variant);
    }

    @Override
    public void delete(Variant variant) {
        Variant entity = entityManager.find(Variant.class, variant.getId());
        remove(entity);
    }


    @Override
    public Variant findByVariantID(String variantID) {

        Variant entity = getSingleResultForQuery(createQuery("select t from Variant t where t.variantID = :variantID")
                .setParameter("variantID", variantID));

        return entity;
    }

    @Override
    public Variant findByVariantIDForDeveloper(String variantID, String loginName) {

        Variant entity = getSingleResultForQuery(createQuery("select t from Variant t where t.variantID = :variantID and t.developer = :developer")
                .setParameter("variantID", variantID)
                .setParameter("developer", loginName));


        return entity;
    }

    @Override
    public List<String> findVariantIDsForDeveloper(String loginName) {
        return createQuery("select t.variantID from Variant t where t.developer = :developer")
                .setParameter("developer", loginName).getResultList();
    }

    @Override
    public List<Variant> findAllVariantsByIDs(List<String> variantIDs) {

        if (variantIDs.isEmpty()) {
            return Collections.emptyList();
        }

        return createQuery("select t from Variant t where t.variantID IN :variantIDs")
                .setParameter("variantIDs", variantIDs).getResultList();
    }

    @Override
    public Variant find(String id) {
        Variant entity = entityManager.find(Variant.class, id);
        return entity;
    }

    private Variant getSingleResultForQuery(Query query) {
        List<Variant> result = query.getResultList();

        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return null;
        }
    }


}
