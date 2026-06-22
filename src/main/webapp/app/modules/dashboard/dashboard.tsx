import React from 'react';

import { SupAdminDashboard } from './dashboard-components';

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isOpenAPIEnabled: boolean;
  currentLocale: string;
}

const Dashboard = () => {
  return (
    <div>
      <SupAdminDashboard />
    </div>
  );
};

export default Dashboard;
