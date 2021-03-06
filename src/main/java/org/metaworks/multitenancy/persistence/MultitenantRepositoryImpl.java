package org.metaworks.multitenancy.persistence;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.metaworks.ObjectInstance;
import org.metaworks.WebObjectType;
import org.metaworks.dwr.MetaworksRemoteService;
import org.metaworks.springboot.configuration.Metaworks4BaseApplication;
import org.oce.garuda.multitenancy.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.modeling.resource.DefaultResource;
import org.uengine.modeling.resource.IResource;
import org.uengine.modeling.resource.ResourceManager;

import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Transactional
public class MultitenantRepositoryImpl<E, PK extends Serializable> extends
        SimpleJpaRepository<E, PK> implements MultitenantRepository<E, PK> {

    private final EntityManager entityManager;
    private final JpaEntityInformation<E, ?> entityInformation;

    public MultitenantRepositoryImpl(final JpaEntityInformation<E, ?> entityInformation,
                                     final EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;


        getEntityManager().setProperty("tenant-id", TenantContext.getThreadLocalInstance().getTenantId());

    }

    @Override
    @Transactional
    public void delete(final E entity) {
        //Assert.notNull(entity, "Entity object must not be null!");
       // entity.setChangeDate(Calendar.getInstance().getTime());
        //entity.setDeleted(true);
        getEntityManager().setProperty("tenant-id", TenantContext.getThreadLocalInstance().getTenantId());

        super.delete(entity);
    }

    @Override
    public List<E> findAll() {


        getEntityManager().setProperty("tenant-id", TenantContext.getThreadLocalInstance().getTenantId());


        return super.findAll();
    }


//    public List<E> findDynamicQuery() {
//
//
//        getEntityManager().setProperty("tenant-id", TenantContext.getThreadLocalInstance().getTenantId());
//
//
//        return super.findAll();
//    }


    @Override
    public E findOne(final PK pk) {
//        return this.findOne(this.isRemovedByID(pk));
        getEntityManager().setProperty("tenant-id", TenantContext.getThreadLocalInstance().getTenantId());

        return super.findOne(pk);
    }

//    private Specification<E> isRemoved() {
//        return new Specification<E>() {
//
//            @Override
//            public Predicate toPredicate(final Root<E> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
//                return cb.isFalse(root.<Boolean> get("deleted"));
//            }
//
//        };
//    }

//    private Specification<E> isRemovedByID(final PK pk) {
//        return new Specification<E>() {
//
//            @Override
//            public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
//                final Predicate id = cb.equal(root.get("id"), pk);
//                final Predicate hidden = cb.isFalse(root.<Boolean> get("deleted"));
//                return cb.and(id, hidden);
//            }
//
//        };
//    }


    private Specification<E> isMyTenantData(final String tenantId) {
        return new Specification<E>() {

            @Override
            public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                final Predicate tenant = cb.equal(root.get("tenantID"), tenantId);
                //final Predicate hidden = cb.isFalse(root.<Boolean> get("deleted"));
                return tenant;//cb.and(id, hidden);
            }

        };
    }

    @Autowired
    private ApplicationContext context;

    public void beforeSave(MultitenantEntity multitenantEntity) {


//        try {
//            if(multitenantEntity.getProps_()!=null){
//                IResource resource = new DefaultResource(_key(multitenantEntity));
//
//                ResourceManager resourceManager = context.getBean(ResourceManager.class);
//                resourceManager.save(resource, multitenantEntity.getProps_());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public void afterLoad(MultitenantEntity multitenantEntity) {
//        String key = _key(multitenantEntity);
//
//        if(key==null) return;
//
//        IResource resource = new DefaultResource(key);
//        try {
//            ResourceManager resourceManager = context.getBean(ResourceManager.class);
//            multitenantEntity.setProps_((Map<String, String>) resourceManager.getObject(resource));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    public String _key(MultitenantEntity multitenantEntity) {

        WebObjectType type = null;
        try {
            String className = multitenantEntity.getClass().getName();
            type = MetaworksRemoteService.getInstance().getMetaworksType(className);

            org.metaworks.Type m2type = type.metaworks2Type();
            ObjectInstance instance = (ObjectInstance)m2type.createInstance();
            instance.setObject(multitenantEntity);
            Object keyFieldValue = instance.getFieldValue(type.getKeyFieldDescriptor().getDisplayName());

            return (className + "@" + keyFieldValue);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public <S extends E> S save(S entity) {

        getEntityManager().setProperty("tenant-id", TenantContext.getThreadLocalInstance().getTenantId());

        if(entity instanceof BeforeSave){
            ((BeforeSave)entity).beforeSave();
        }

        if(entity instanceof MultitenantEntity){
            beforeSave((MultitenantEntity) entity);
        }

        return super.save(entity);
    }

    @Override
    public Page<E> findAll(Pageable pageable) {

        getEntityManager().setProperty("tenant-id", TenantContext.getThreadLocalInstance().getTenantId());

        Page<E> page = super.findAll(pageable);

        for(E entity: page.getContent()){

            if(entity instanceof AfterLoad) {
                ((AfterLoad) entity).afterLoad();
            }

            if(entity instanceof MultitenantEntity){
                afterLoad((MultitenantEntity) entity);
            }

        }

        return page;
    }

    @Override
    public List<E> findAll(Iterable<PK> pks) {
        return super.findAll(pks);
    }

    @Override
    public List<E> findAll(Sort sort) {
        return super.findAll(sort);
    }

    @Override
    public E findOne(Specification<E> spec) {
        return super.findOne(spec);
    }

    @Override
    public List<E> findAll(Specification<E> spec) {
        return super.findAll(spec);
    }

    @Override
    public Page<E> findAll(Specification<E> spec, Pageable pageable) {
        return super.findAll(spec, pageable);
    }

    @Override
    public List<E> findAll(Specification<E> spec, Sort sort) {
        return super.findAll(spec, sort);
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    protected JpaEntityInformation<E, ?> getEntityInformation() {
        return this.entityInformation;
    }



}