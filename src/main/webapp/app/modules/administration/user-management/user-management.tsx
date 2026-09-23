import React, { useEffect, useState } from 'react';
import { Badge, Button, Col, Table } from 'react-bootstrap';
import { JhiItemCount, JhiPagination, Translate, getPaginationState, ValidatedInput } from 'react-jhipster';
import { Link, useLocation, useNavigate } from 'react-router';
import './user-management.scss';

import { faPencilAlt, faPlus, faSearch, faTrash } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';

import { getUsersAsAdmin, updateUser } from './user-management.reducer';
import LinkButton from 'app/shared/components/link-button';

export const UserManagement = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [pagination, setPagination] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const getUsersFromProps = () => {
    dispatch(
      getUsersAsAdmin({
        page: pagination.activePage - 1,
        size: pagination.itemsPerPage,
        sort: `${pagination.sort},${pagination.order}`,
      }),
    );
    const endURL = `?page=${pagination.activePage}&sort=${pagination.sort},${pagination.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    getUsersFromProps();
  }, [pagination.activePage, pagination.order, pagination.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sortParam = params.get(SORT);
    if (page && sortParam) {
      const sortSplit = sortParam.split(',');
      setPagination({
        ...pagination,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () =>
    setPagination({
      ...pagination,
      order: pagination.order === ASC ? DESC : ASC,
      sort: p,
    });

  const handlePagination = currentPage =>
    setPagination({
      ...pagination,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    getUsersFromProps();
  };

  const toggleActive = user => () => {
    dispatch(
      updateUser({
        ...user,
        activated: !user.activated,
      }),
    );
  };

  const account = useAppSelector(state => state.authentication.account);
  const users = useAppSelector(state => state.userManagement.users);
  const authorities = useAppSelector(state => state.userManagement.authorities);
  const totalItems = useAppSelector(state => state.userManagement.totalItems);
  {
    /*
    const getSortIconByFieldName = (fieldName: string) => {
      const sortFieldName = pagination.sort;
      const {order} = pagination;
      if (sortFieldName !== fieldName) {
        return faSort;
      }
      return order === ASC ? faSortUp : faSortDown;
    };
  */
  }

  return (
    <div>
      <h2 id="user-management-page-heading" data-cy="UserManagementHeading">
        <Translate contentKey="userManagement.home.title">User Management</Translate>
      </h2>
      <p>
        Administra el acceso y roles de los usuarios del sistema. Crea, edita o desactiva cuentas según los requerimientos institucionales.
      </p>
      <Col className="d-flex justify-content-between align-items-center" md="12">
        <div className="d-flex align-items-center searchBar">
          <div className="d-flex align-items-center w-50">
            <FontAwesomeIcon icon={faSearch}></FontAwesomeIcon>
            <ValidatedInput name="search" placeholder="Buscar por nombre, email o documento..." />
          </div>
          <ValidatedInput type="select" name="state" className="w-25">
            {authorities.map(rol => (
              <option value={rol} key={rol}>
                {rol}
              </option>
            ))}
          </ValidatedInput>
        </div>
        <LinkButton to="new" data-cy="entityCreateButton">
          <FontAwesomeIcon icon={faPlus} /> <Translate contentKey="userManagement.home.createLabel">Create a new user</Translate>
        </LinkButton>
      </Col>
      <Table responsive striped>
        <thead>
          <tr>
            <th className="hand" onClick={sort('id')}>
              Nombre
            </th>
            <th className="hand" onClick={sort('id')}>
              Documento
            </th>
            <th className="hand" onClick={sort('email')}>
              Email
            </th>
            <th>Rol</th>
            <th>Estado</th>
            <th id="modified-date-sort" className="hand">
              Acciones
            </th>
          </tr>
        </thead>
        <tbody>
          {users.map((user, i) => (
            <tr id={user.login} key={`user-${i}`} data-cy="entityTable">
              <td>{user.fullName}</td>
              <td>{user.documentNumber}</td>
              <td>{user.email}</td>
              <td>
                {user.authorities?.map((authority, j) => (
                  <div key={`user-auth-${i}-${j}`}>
                    <Badge bg="info">{authority}</Badge>
                  </div>
                ))}
              </td>
              <td>
                {user.activated ? (
                  <Button variant="success" onClick={toggleActive(user)}>
                    <Translate contentKey="userManagement.activated">Activated</Translate>
                  </Button>
                ) : (
                  <Button variant="danger" onClick={toggleActive(user)}>
                    <Translate contentKey="userManagement.deactivated">Deactivated</Translate>
                  </Button>
                )}
              </td>
              <td className="text-end">
                <div className="btn-group flex-btn-group-container">
                  {/* <Button as={Link as any} to={user.login} variant="info" size="sm" data-cy="entityDetailsButton">
                    <FontAwesomeIcon icon={faEye}/>{' '}
                    <span className="d-none d-md-inline">
                      <Translate contentKey="entity.action.view">View</Translate>
                    </span>
                  </Button>*/}
                  <Button as={Link as any} to={`${user.login}/edit`} variant="primary" size="sm" data-cy="entityEditButton">
                    <FontAwesomeIcon icon={faPencilAlt} />{' '}
                    <span className="d-none d-md-inline">
                      <Translate contentKey="entity.action.edit">Edit</Translate>
                    </span>
                  </Button>
                  <Button
                    as={Link as any}
                    to={`${user.login}/delete`}
                    variant="danger"
                    size="sm"
                    disabled={account.login === user.login}
                    data-cy="entityDeleteButton"
                  >
                    <FontAwesomeIcon icon={faTrash} />{' '}
                    <span className="d-none d-md-inline">
                      <Translate contentKey="entity.action.delete">Delete</Translate>
                    </span>
                  </Button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
      {totalItems && (
        <div className={users?.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={pagination.activePage} total={totalItems} itemsPerPage={pagination.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={pagination.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={pagination.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;
