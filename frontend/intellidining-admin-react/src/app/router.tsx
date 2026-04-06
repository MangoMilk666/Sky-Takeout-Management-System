import { Navigate, createHashRouter } from 'react-router-dom'
import { RequireAuth } from '@/layout/RequireAuth'
import { RootLayout } from '@/layout/RootLayout'
import { CategoryPage } from '@/pages/category/CategoryPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { DishAddPage } from '@/pages/dish/DishAddPage'
import { DishPage } from '@/pages/dish/DishPage'
import { EmployeeAddPage } from '@/pages/employee/EmployeeAddPage'
import { EmployeePage } from '@/pages/employee/EmployeePage'
import { LoginPage } from '@/pages/login/LoginPage'
import { NotFoundPage } from '@/pages/notfound/NotFoundPage'
import { OrderPage } from '@/pages/order/OrderPage'
import { SetmealAddPage } from '@/pages/setmeal/SetmealAddPage'
import { SetmealPage } from '@/pages/setmeal/SetmealPage'
import { StatisticsPage } from '@/pages/statistics/StatisticsPage'

export const router = createHashRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/404', element: <NotFoundPage /> },
  {
    element: <RequireAuth />,
    children: [
      {
        element: <RootLayout />,
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: '/dashboard', element: <DashboardPage /> },
          { path: '/statistics', element: <StatisticsPage /> },
          { path: '/order', element: <OrderPage /> },
          { path: '/setmeal', element: <SetmealPage /> },
          { path: '/setmeal/add', element: <SetmealAddPage /> },
          { path: '/dish', element: <DishPage /> },
          { path: '/dish/add', element: <DishAddPage /> },
          { path: '/category', element: <CategoryPage /> },
          { path: '/employee', element: <EmployeePage /> },
          { path: '/employee/add', element: <EmployeeAddPage /> },
        ],
      },
    ],
  },
  { path: '*', element: <Navigate to="/404" replace /> },
])
