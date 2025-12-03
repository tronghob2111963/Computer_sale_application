export interface BuildCategory {
    id: string;
    name: string;
    icon: string;
    categoryId?: string;
}

export const BUILD_CATEGORIES: BuildCategory[] = [
    { id: 'cpu', name: 'CPU', icon: '🖥️' },
    { id: 'mainboard', name: 'MAINBOARD', icon: '🔧' },
    { id: 'ram', name: 'RAM', icon: '💾' },
    { id: 'gpu', name: 'CARD ĐỒ HỌA', icon: '🎮' },
    { id: 'storage', name: 'Ổ CỨNG', icon: '💿' }
];
