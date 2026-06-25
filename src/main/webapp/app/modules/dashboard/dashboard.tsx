import React from 'react';

import { AprenticeDashboard, InstructorDashboard, SupAdminDashboard } from './dashboard-components';

export interface IDashboardProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  isInstructor: boolean;
  isAprentice: boolean;
}

const Dashboard = (props: IDashboardProps) => {
  return (
    <div>
      {props.isAuthenticated && props.isAdmin && <SupAdminDashboard />}
      {props.isAuthenticated && props.isInstructor && <InstructorDashboard />}
      {props.isAuthenticated && props.isAprentice && <AprenticeDashboard />}
    </div>
  );
};

export default Dashboard;
